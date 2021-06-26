package top.someones.cardmatch.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import top.someones.cardmatch.R
import top.someones.cardmatch.databinding.FragmentUserBinding
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.service.UserService
import top.someones.cardmatch.ui.main.MainActivity

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserBinding.bind(view)
        binding.btnLogin.setOnClickListener {
            val user: String = binding.editUser.text.toString()
            val pass: String = binding.editPass.text.toString()
            if ("null" == user || user.length < 3) {
                Toast.makeText(requireContext(), "用户名最少3位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if ("null" == pass || pass.length < 4) {
                Toast.makeText(requireContext(), "密码最少4位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            UserService.login(requireContext(), user, pass) {
                UserData.addData(requireContext(), it.uid, it.username, it.session!!)
                Toast.makeText(requireContext(), "登录成功，欢迎回来...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        }
        binding.btnRegister.setOnClickListener {
            val user: String = binding.editUser.text.toString()
            val pass: String = binding.editPass.text.toString()
            if ("null" == user || user.length < 3) {
                Toast.makeText(requireContext(), "用户名最少3位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if ("null" == pass || pass.length < 4) {
                Toast.makeText(requireContext(), "密码最少4位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            UserService.register(requireContext(), user, pass) {
                Toast.makeText(requireContext(), "欢迎${it.username}加入", Toast.LENGTH_SHORT).show()
                UserData.addData(requireContext(), it.uid, it.username, it.session!!)
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}