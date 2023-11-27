package hisnet.login

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import kotlin.collections.HashMap

class AuthService {

    @Value("\${custom.hisnet.secret-key}")
    private lateinit var secretKey: String

    @Value("\${custom.hisnet.secret-iv}")
    private lateinit var secretIv: String

    fun hisnetLogin(form: LoginForm): Map<String, Any> {
        val encryptedForm = encryptLoginForm(form)
        return callHisnetLoginApi(encryptedForm)
    }

    fun encryptLoginForm(form: LoginForm): LoginForm {
        val encryptedUniqueId = encryptAES256CBC(form.uniqueId)
        val encryptedPassword = encryptAES256CBC(form.password)

        return LoginForm(encryptedUniqueId, encryptedPassword)
    }

    fun encryptAES256CBC(plainText: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
            val ivParameterSpec = IvParameterSpec(secretIv.toByteArray())

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray())

            return Base64.getEncoder().encodeToString(encrypted)
        } catch (e: Exception) {
            throw RuntimeException("Error encrypting the text: ${e.message}", e)
        }
    }

    private fun callHisnetLoginApi(encryptedForm: LoginForm): Map<String, Any> {
        val result = HashMap<String, Any>()

        val factory = HttpComponentsClientHttpRequestFactory()

        factory.setConnectTimeout(5000)
        factory.setReadTimeout(5000)

        val restTemplate = RestTemplate(factory)

        val header = org.springframework.http.HttpHeaders()
        val entity = HttpEntity<Map<String, Any>>(header)
        val url = "https://hisnet.handong.edu/api/hdOAC/index.php/user/login"
        val uri: UriComponents =
            UriComponentsBuilder.fromHttpUrl("$url?usernum=${encryptedForm.uniqueId}&userpw=${encryptedForm.password}")
                .build()

        val resultMap: ResponseEntity<Map<*, *>> =
            restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Map::class.java)

        result["statusCode"] = resultMap.body?.get("status") ?: -1
        result["description"] = resultMap.body?.get("description") ?: ""

        // 상태 코드에 따른 처리
        when (result["statusCode"]) {
            200 -> {
                val resultData = resultMap.body?.get("result") as? List<Map<String, Any>>?
                resultData?.let { result["result"] = it.firstOrNull() ?: emptyMap<String, Any>() }
            }
            400, 401, 404, 405 -> {
                throw HisnetLoginFailException(result["statusCode"] as Int, result["description"] as String)
            }
            else -> {
                throw MileageException("Unknown Status Code: ${result["statusCode"]}, ${result["description"]}")
            }
        }

        return result["result"] as Map<String, Any>
    }
}
