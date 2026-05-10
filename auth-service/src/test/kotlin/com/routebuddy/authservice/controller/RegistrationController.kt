package com.routebuddy.authservice.controller

import com.routebuddy.authservice.config.JwtCookieFactory
import com.routebuddy.authservice.config.JwtTokenProvider
import com.routebuddy.authservice.config.RedirectUrlValidator
import com.routebuddy.authservice.dto.RegistrationRequest
import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import com.routebuddy.authservice.service.AuthService
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
        controller = RegistrationController(
            authService,
            userRepository,
            jwtTokenProvider,
            JwtCookieFactory(jwtTokenProvider),
            RedirectUrlValidator()
        )
    }

    @Test
    fun `showRegistrationForm should return register view and set model attributes`() {
        val request = MockHttpServletRequest()
        request.setParameter("redirectUrl", "/constructor")
        val model = ConcurrentModel()

        val viewName = controller.showRegistrationForm(model, request)
        assertEquals("register", viewName)
        assertEquals("/constructor", model.getAttribute("redirectUrl"))
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
        whenever(authService.register(any())).thenThrow(RuntimeException("Имя пользователя уже занято"))

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)
        assertEquals("register", viewName)
        assertEquals("Имя пользователя уже занято", model.getAttribute("error"))
    }

    @Test
    fun `processRegistrationForm should set JWT cookie and redirect when successful`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        request.setParameter("redirectUrl", "/constructor")
        request.addHeader("X-Forwarded-Proto", "https")
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

        val cookieHeader = response.getHeader("Set-Cookie")!!
        assertTrue(cookieHeader.contains("JWT=dummyJWT"))
        assertTrue(cookieHeader.contains("HttpOnly"))
        assertTrue(cookieHeader.contains("Path=/"))
        assertTrue(cookieHeader.contains("Max-Age=3600"))
        assertTrue(cookieHeader.contains("SameSite=Lax"))
        assertTrue(cookieHeader.contains("Secure"))

        assertEquals("redirect:/constructor?nickname=user", viewName)
    }

    @Test
    fun `processRegistrationForm should ignore external redirect url`() {
        val registrationRequest = RegistrationRequest("user", "pass", "USER", "user@test.com")
        val bindingResult: BindingResult = BeanPropertyBindingResult(registrationRequest, "registrationRequest")

        val model = ConcurrentModel()
        val request = MockHttpServletRequest()
        request.setParameter("redirectUrl", "https://example.com/game")
        val response = MockHttpServletResponse()

        doNothing().whenever(authService).register(any())
        val user = User(1, "user", "encoded", "USER", "user@test.com")
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn("dummyJWT")
        whenever(jwtTokenProvider.getJwtExpirationInMs()).thenReturn(3600000L)

        val viewName = controller.processRegistrationForm(registrationRequest, bindingResult, model, request, response)

        assertEquals("redirect:/profile/user", viewName)
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
        assertEquals("redirect:/profile/user", viewName)
    }
}
