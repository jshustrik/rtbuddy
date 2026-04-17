package com.routebuddy.reviews.controller

import com.routebuddy.reviews.dto.ReviewRequest
import com.routebuddy.reviews.dto.ReviewUpdateRequest
import com.routebuddy.reviews.service.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/routes/{routeId}/reviews")
class ViewReviewController(private val reviewService: ReviewService) {

    @GetMapping
    fun reviewsPage(
        @PathVariable routeId: Long,
        @RequestParam(required = false) routeTitle: String?,
        @RequestParam(required = false) routeDescription: String?,
        @RequestHeader("X-User-Id", defaultValue = "0") userId: Long,
        model: Model
    ): String {
        val reviewPage = reviewService.getReviewsByRouteId(routeId, PageRequest.of(0, 100))
        val rating = reviewService.getRouteRating(routeId)
        val userHasReview = reviewPage.content.any { it.authorId == userId }

        model.addAttribute("routeId", routeId)
        model.addAttribute("routeTitle", routeTitle ?: "Маршрут #$routeId")
        model.addAttribute("routeDescription", routeDescription ?: "Описание маршрута пока не заполнено.")
        model.addAttribute("reviews", reviewPage.content)
        model.addAttribute("avgRating", rating.averageRating)
        model.addAttribute("reviewsCount", rating.reviewsCount)
        model.addAttribute("userHasReview", userHasReview)
        model.addAttribute("userId", userId)
        return "review"
    }

    @PostMapping
    fun createReviewFromView(
        @PathVariable routeId: Long,
        @RequestHeader("X-User-Id") userId: Long,
        @RequestHeader("X-Username", defaultValue = "anonymous") username: String,
        @RequestParam("comment") comment: String,
        @RequestParam("rating") rating: Int,
        redirect: RedirectAttributes
    ): String {
        reviewService.createReview(userId, username, ReviewRequest(routeId = routeId, text = comment, rating = rating))
        redirect.addFlashAttribute("message", "Отзыв успешно добавлен")
        return "redirect:/routes/$routeId/reviews"
    }

    @GetMapping("/{reviewId}/edit")
    fun editReviewPage(
        @PathVariable routeId: Long,
        @PathVariable reviewId: String,
        model: Model
    ): String {
        val reviews = reviewService.getReviewsByRouteId(routeId, PageRequest.of(0, 100)).content
        val review = reviews.firstOrNull { it.id == reviewId } ?: throw IllegalArgumentException("Отзыв не найден")
        model.addAttribute("routeId", routeId)
        model.addAttribute("review", review)
        return "review-edit"
    }

    @PutMapping("/{reviewId}")
    fun updateReviewFromView(
        @PathVariable routeId: Long,
        @PathVariable reviewId: String,
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("comment") comment: String,
        @RequestParam("rating") rating: Int,
        redirect: RedirectAttributes
    ): String {
        reviewService.updateReview(reviewId, userId, ReviewUpdateRequest(text = comment, rating = rating))
        redirect.addFlashAttribute("message", "Отзыв обновлен")
        return "redirect:/routes/$routeId/reviews"
    }

    @DeleteMapping("/{reviewId}")
    fun deleteReviewFromView(
        @PathVariable routeId: Long,
        @PathVariable reviewId: String,
        @RequestHeader("X-User-Id") userId: Long,
        redirect: RedirectAttributes
    ): String {
        reviewService.deleteReview(reviewId, userId)
        redirect.addFlashAttribute("message", "Отзыв удален")
        return "redirect:/routes/$routeId/reviews"
    }
}
