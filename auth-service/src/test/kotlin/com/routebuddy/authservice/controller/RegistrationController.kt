package com.routebuddy.authservice.controller

import com.quizwhiz.authservice.dto.RegistrationRequest
import com.quizwhiz.authservice.model.User
import com.quizwhiz.authservice.repository.UserRepository
import com.quizwhiz.authservice.service.AuthService
import com.quizwhiz.authservice.config.JwtTokenProvider
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.ui.ConcurrentModel
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult

class RegistrationControllerTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var controller: RegistrationController

    @BeforeEach
    fun setUp() {
        authService = mock()
        userRepository = mock()
        jwtTokenProvider = mock()
        controller = RegistrationController(authService, userRepository, jwtTokenProvider)
    }

    @Test
    fun `showRegistrationForm should return register view and set model attributes`() {
        val request = MockHttpServletRequest()
        request.setParameter("redirectUrl", "http://example.com/redirect")
        val model = ConcurrentModel()

        val viewName = controller.showRegistrationForm(model, request)
        assertEquals("register", viewName)
        assertEquals("http://example.com/redirect", model.getAttribute("redirectUrl"))
        assertNotNull(model.getAttribute("registrationRequest"))
    }

    @Test
    fun `processRegistrationForm should return register if binding has errors`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")
        bindingResult.reject("error", "Some validation error")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)
        assertEquals("register", viewName)
    }

    @Test
    fun `processRegistrationForm should handle exception and return register view with error`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        // эмулируем ошибку при регистрации
        whenever(authService.register(any())).thenThrow(RuntimeException("Username already exists"))

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)
        assertEquals("register", viewName)
        assertEquals("Username already exists", model.getAttribute("error"))
    }

    @Test
    fun `processRegistrationForm should set JWT cookie and redirect when successful`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        request.setParameter("redirectUrl", "http://example.com/game")
        val response = MockHttpServletResponse()

        // Успешная регистрация
        doNothing().whenever(authService).register(any())
        // После регистрации пользователь появляется в репозитории
        val user = User(1, "user", "encoded", "USER", "user@test.com")
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        // Мокаем jwtTokenProvider
        whenever(jwtTokenProvider.generateToken(user)).thenReturn("dummyJWT")
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)

        // Проверка Cookie
        val cookie: Cookie? = response.cookies.find { it.name == "JWT" }
        assertNotNull(cookie)
        assertEquals("dummyJWT", cookie!!.value)
        assertTrue(cookie.isHttpOnly)
        assertEquals("/", cookie.path)
        // 3600000 мс = 3600 секунд
        assertEquals(3600, cookie.maxAge)

        // Проверяем, что перенаправление корректное
        assertEquals("redirect:http://example.com/game?nickname=user", viewName)
    }

    @Test
    fun `processRegistrationForm should redirect to default profile url if no redirectUrl`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        doNothing().whenever(authService).register(any())
        val user = User(1, "user", "encoded", "USER", "user@test.com")
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn("dummyJWT")
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)
        assertEquals("redirect:http://127.0.0.1:8082/profile/user", viewName)
    }
}
