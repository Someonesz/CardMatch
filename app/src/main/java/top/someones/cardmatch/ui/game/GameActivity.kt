package top.someones.cardmatch.ui.game

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import okhttp3.*
import top.someones.cardmatch.R
import top.someones.cardmatch.core.DatabaseHelper
import top.someones.cardmatch.core.GameCallback
import top.someones.cardmatch.core.GameManagement
import top.someones.cardmatch.core.GameObserver
import top.someones.cardmatch.databinding.ActivityGameBinding
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.service.GameService
import top.someones.cardmatch.ui.BaseActivity
import top.someones.cardmatch.util.NetUtil
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GameActivity : BaseActivity() {
    private var mGameUUID: String? = null
    private var mGameObserver: GameObserver? = null
    private lateinit var mDatabaseHelper: SQLiteOpenHelper
    private val sdf = SimpleDateFormat.getDateTimeInstance()
    private val mRankDialogSize = Point()
    private val mTimeHandler = Handler()
    private var mSelect1 = -1
    private var mSelect2 = -1
    private var mGameSteps = 0
    private var mGameTime = 0
    private lateinit var mGameTimer: Runnable
    private lateinit var mGameToken: String
    private lateinit var mViewBinding: ActivityGameBinding
    private lateinit var mCells: List<Cell>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        //初始化游戏观察者
        try {
            mGameUUID = intent.getStringExtra("uuid")
            if (mGameUUID == null) throw Exception("游戏UUID为空")
            mGameObserver = GameManagement.getGameObserver(mGameUUID!!, this, Callback())
            if (mGameObserver == null) throw Exception("游戏GM初始化失败")
        } catch (e: Exception) {
            Toast.makeText(this, "游戏初始化失败 ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
        mDatabaseHelper = DatabaseHelper(this)
        windowManager.defaultDisplay.getSize(mRankDialogSize)
        mRankDialogSize.x = (0.85 * mRankDialogSize.x).toInt()
        mRankDialogSize.y = (0.8 * mRankDialogSize.y).toInt()
        immersionStatusBar()

        //绑定按钮
        mCells = listOf<Cell>(
            mViewBinding.c1,
            mViewBinding.c2,
            mViewBinding.c3,
            mViewBinding.c4,
            mViewBinding.c5,
            mViewBinding.c6,
            mViewBinding.c7,
            mViewBinding.c8,
            mViewBinding.c9,
            mViewBinding.c10,
            mViewBinding.c11,
            mViewBinding.c12,
            mViewBinding.c13,
            mViewBinding.c14,
            mViewBinding.c15,
            mViewBinding.c16
        )

        //绑定事件
        for (i in mCells.indices) {
            mCells[i].setOnClickListener {
                if (mSelect1 == -1) {
                    mSelect1 = i
                    mCells[i].showBorder()
                } else {
                    if (mSelect1 == i) {
                        mSelect1 = -1
                        mCells[i].hideBorder()
                    } else {
                        mSelect2 = i
                        mCells[i].showBorder()
                        submit()
                    }
                }
            }
        }

        //新游戏
        mViewBinding.actionRestart.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mViewBinding.actionRestart.setImageResource(R.drawable.action_restart_touch)
                MotionEvent.ACTION_UP -> mViewBinding.actionRestart.setImageResource(R.drawable.action_restart)
            }
            false
        }
        mViewBinding.actionRestart.setOnClickListener { newGame() }

        //暂停计时
        mViewBinding.actionPause.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mViewBinding.actionPause.setImageResource(R.drawable.action_pause_touch)
                MotionEvent.ACTION_UP -> mViewBinding.actionPause.setImageResource(R.drawable.action_pause)
            }
            false
        }
        mViewBinding.actionPause.setOnClickListener {
            if (mViewBinding.gamePauseInfo.visibility == View.VISIBLE) {
                mViewBinding.gamePauseInfo.visibility = View.INVISIBLE
                mTimeHandler.postDelayed(mGameTimer, 0)
            } else {
                mViewBinding.gamePauseInfo.visibility = View.VISIBLE
                mTimeHandler.removeCallbacks(mGameTimer)
            }
        }

        //展示排名
        mViewBinding.actionRank.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mViewBinding.actionRank.setImageResource(R.drawable.action_rank_touch)
                MotionEvent.ACTION_UP -> mViewBinding.actionRank.setImageResource(R.drawable.action_rank)
            }
            false
        }
        mViewBinding.actionRank.setOnClickListener {
            if (mViewBinding.gamePauseInfo.visibility != View.VISIBLE && mViewBinding.gameWinInfo.visibility != View.VISIBLE) mViewBinding.actionPause.performClick()
            GameRankDialog(
                this@GameActivity,
                mGameUUID!!,
                mDatabaseHelper,
                mRankDialogSize
            ).show()
        }

        //放弃并返回主页
        mViewBinding.actionExit.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mViewBinding.actionExit.setImageResource(R.drawable.action_exit_touch)
                MotionEvent.ACTION_UP -> mViewBinding.actionExit.setImageResource(R.drawable.action_exit)
            }
            false
        }
        mViewBinding.actionExit.setOnClickListener { finish() }

        //消耗点击事件，阻止事件向下传递
        mViewBinding.gamePauseInfo.setOnClickListener(null)
        mViewBinding.gameWinInfo.setOnClickListener(null)
        mGameTimer = Timer()
        newGame()
    }

    private fun newGame() {
        mGameToken = UUID.randomUUID().toString()
        mSelect1 = -1
        mSelect2 = -1
        mGameSteps = 0
        val views = mGameObserver!!.newGame(mGameToken)
        for (i in mCells.indices) {
            mCells[i].setView(views[i][0], views[i][1])
        }
        mViewBinding.gameSteps.text = 0.toString()
        mGameTime = -1
        mViewBinding.actionPause.isEnabled = true
        mViewBinding.actionRank.isEnabled = true
        mViewBinding.gamePauseInfo.visibility = View.INVISIBLE
        mViewBinding.gameWinInfo.visibility = View.INVISIBLE
        mTimeHandler.removeCallbacks(mGameTimer)
        mTimeHandler.postDelayed(mGameTimer, 0)
    }

    private fun submit() {
        mCells[mSelect1].showNext()
        mCells[mSelect2].showNext()
        mGameObserver!!.check(mSelect1, mSelect2)
        mSelect1 = -1
        mSelect2 = -1
        mViewBinding.gameSteps.text = (++mGameSteps).toString()
    }

    override fun onDestroy() {
        mTimeHandler.removeCallbacks(mGameTimer)
        mDatabaseHelper.close()
        super.onDestroy()
    }

    private inner class Callback : GameCallback {
        override fun onSuccess(token: String, arg1: Int, arg2: Int) {
            Thread {
                try {
                    Thread.sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                runOnUiThread {
                    if (token != mGameToken) return@runOnUiThread
                    mCells[arg1].fadeOut()
                    mCells[arg2].fadeOut()
                }
            }.start()
        }

        override fun onFailure(token: String, arg1: Int, arg2: Int) {
            Thread {
                try {
                    Thread.sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                runOnUiThread {
                    if (token != mGameToken) return@runOnUiThread
                    mCells[arg1].showNext()
                    mCells[arg2].showNext()
                    mCells[arg1].hideBorder()
                    mCells[arg2].hideBorder()
                }
            }.start()
        }

        override fun onWin(token: String) {
            mTimeHandler.removeCallbacks(mGameTimer)
            mViewBinding.actionPause.isEnabled = false
            mViewBinding.gameFinalTime.text = "用时:$mGameTime"
            mViewBinding.gameFinalSteps.text = "步数:$mGameSteps"
            val score = getScore(mGameSteps, mGameTime)
            mViewBinding.gameFinalScore.text = "分数:$score"
            mViewBinding.gameWinInfo.visibility = View.VISIBLE
            try {
                mDatabaseHelper.writableDatabase.use { db ->
                    db.execSQL(
                        "INSERT INTO GameHistory (uuid,time,score) VALUES (?,?,?)", arrayOf<Any?>(
                            mGameUUID, sdf.format(
                                System.currentTimeMillis()
                            ), score
                        )
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this@GameActivity, "添加记录失败", Toast.LENGTH_SHORT).show()
            }
            GameService.addScore(this@GameActivity, mGameUUID!!, score, {
                Toast.makeText(this@GameActivity, "成绩成功提交到服务器", Toast.LENGTH_SHORT).show()
            }, {
                Toast.makeText(this@GameActivity, "提交成绩失败：${it}", Toast.LENGTH_SHORT).show()
            })
        }

        private fun getScore(steps: Int, time: Int): Int {
            val last = 9.0 / ((steps - 3) * 5 + time)
            return (last * 100000).toInt()
        }
    }

    private inner class Timer : Runnable {
        override fun run() {
            mViewBinding.gameTime.text = (++mGameTime).toString()
            mTimeHandler.postDelayed(this, 1000)
        }
    }

}