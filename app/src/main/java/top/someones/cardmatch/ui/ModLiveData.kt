package top.someones.cardmatch.ui

import androidx.lifecycle.LiveData
import top.someones.cardmatch.entity.Mod

class ModLiveData private constructor() : LiveData<Array<Mod>?>() {
    public override fun postValue(value: Array<Mod>?) {
        super.postValue(value)
    }

    public override fun setValue(value: Array<Mod>?) {
        super.setValue(value)
    }

    companion object {
        val liveData = ModLiveData()
    }
}