package com.routebuddy.authservice.controller

import com.routebuddy.authservice.dto.RegistrationRequest
import com.routebuddy.authservice.model.User
import com.routebuddy.authservice.repository.UserRepository
import com.routebuddy.authservice.service.AuthService
import com.routebuddy.authservice.config.JwtCookieFactory
import com.routebuddy.authservice.config.JwtTokenProvider
import com.routebuddy.authservice.config.RedirectUrlValidator
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RegistrationController(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtCookieFactory: JwtCookieFactory,
    private val redirectUrlValidator: RedirectUrlValidator
) {

    @GetMapping("/register")
    fun showRegistrationForm(model: Model, request: HttpServletRequest): String {
        val redirectUrl = redirectUrlValidator.sanitize(request.getParameter("redirectUrl"))
        model.addAttribute("redirectUrl", redirectUrl)
        model.addAttribute("registrationRequest", RegistrationRequest("", "", "USER", ""))
        return "register"
    }

    @PostMapping("/register")
    fun processRegistrationForm(
        @Valid @ModelAttribute registrationRequest: RegistrationRequest,
        bindingResult: BindingResult,
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        if (bindingResult.hasErrors()) {
            return "register"
        }
        try {
            authService.register(registrationRequest)
            val user: User = userRepository.findByUsername(registrationRequest.username)
                ?: throw RuntimeException("User not found after registration")
            val token = jwtTokenProvider.generateToken(user)
            jwtCookieFactory.addCookie(request, response, token)
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            return "register"
        }
        val redirectUrl = redirectUrlValidator.sanitize(request.getParameter("redirectUrl"))
        val targetUrl = redirectUrl
            ?.let { redirectUrlValidator.withNickname(it, registrationRequest.username) }
            ?: redirectUrlValidator.profileUrl(registrationRequest.username)
        return "redirect:$targetUrl"
    }
}
