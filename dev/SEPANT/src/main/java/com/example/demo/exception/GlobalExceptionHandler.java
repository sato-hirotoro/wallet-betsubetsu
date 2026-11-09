package com.example.demo.exception;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * 例外のハンドリング（処理設計書 2.2）。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final MessageSource messageSource;

	public GlobalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/** 精算済みの月の変更 → ホームへリダイレクトして E701 を表示する。 */
	@ExceptionHandler(SeisanzumiException.class)
	public String handleSeisanzumi(SeisanzumiException e, HttpServletRequest request) {
		RequestContextUtils.getOutputFlashMap(request)
				.put("errorMessage", messageSource.getMessage("E701", null, Locale.JAPAN));
		return "redirect:/home?ym=" + e.getYm();
	}

	/** 不正なパラメータ → エラー画面（E901）。 */
	@ExceptionHandler({ InvalidRequestException.class, MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class })
	public ModelAndView handleInvalidRequest(Exception e) {
		log.warn("不正なリクエスト: {}", e.getMessage());
		ModelAndView mav = new ModelAndView("error");
		mav.addObject("errorMessage", messageSource.getMessage("E901", null, Locale.JAPAN));
		mav.setStatus(HttpStatus.BAD_REQUEST);
		return mav;
	}
}
