package com.omnyom.yumyum

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.omnyom.yumyum.databinding.ActivityLoginBinding
import com.omnyom.yumyum.databinding.ActivitySplashBinding
import com.omnyom.yumyum.helper.GoogleLoginHelper
import com.omnyom.yumyum.helper.KakaoMapUtils
import com.omnyom.yumyum.helper.PreferencesManager
import com.omnyom.yumyum.helper.RetrofitManager
import com.omnyom.yumyum.model.login.LoginResponse
import com.omnyom.yumyum.ui.base.BaseBindingActivity
import com.omnyom.yumyum.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : BaseBindingActivity<ActivitySplashBinding>(R.layout.activity_splash) {
    private lateinit var sharedPref: SharedPreferences

    override fun extraSetupBinding() { }

    override fun setup() {
        startSplash()
    }

    override fun setupViews() {
        supportActionBar?.hide()
    }

    override fun onSubscribe() { }

    override fun release() { }

    override fun permissionGranted(requestCode: Int) {
        sharedPref = this?.getPreferences(MODE_PRIVATE) ?: return

        if (GoogleLoginHelper.firebaseAuth.currentUser == null) {
            PreferencesManager.setString(this, getString(R.string.saved_google_email), "")
            startLoginActivity()
        } else {
            login(GoogleLoginHelper.firebaseAuth.currentUser.email, { startMainActivity() }, { Toast.makeText(this, "로그인이 불안정합니다.", Toast.LENGTH_LONG).show()})
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            KakaoMapUtils.PERM_COARSE_LOCATION -> {
                Toast.makeText(baseContext, "위치 권한을 승인해야 지도를 사용할 수 있습니다! [COARSE_LOCATION]", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
            KakaoMapUtils.PERM_FINE_LOCATION -> {
                Toast.makeText(baseContext, "위치 권한을 승인해야 지도를 사용할 수 있습니다! [FINE_LOCATION]", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
        }
    }

    private fun startSplash() {
        Handler().postDelayed({
            requirePermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), KakaoMapUtils.PERM_FINE_LOCATION)
        }, 2000)
    }

    private fun login(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val call = RetrofitManager.retrofitService.login(email)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful) {
                    PreferencesManager.setLong(application, "userId", response.body()?.data!!.id)
                    onSuccess()
                }
                else {
                    when (response.code()) {
                        404 -> onFailure()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onFailure()
            }
        })
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).let {
            startActivity(it)
            finish()
        }
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).let {
            startActivity(it)
            finish()
        }
    }
}