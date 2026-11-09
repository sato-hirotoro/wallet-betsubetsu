package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * 入力エラーを画面に表示するメッセージ（「{行番号}行目:〜」）に変換する。
 */
@Component
public class RowErrorMessages {

	private static final Pattern ROW_FIELD = Pattern.compile("^(\\w+)\\[(\\d+)\\]\\.(\\w+)$");

	private static final Map<String, String> LABELS = Map.of(
			"shiharaiDd", "支払日",
			"kingaku", "金額",
			"biko", "備考",
			"himokuCd", "費目",
			"userId", "利用者",
			"kaishiMm", "開始月",
			"shuryoMm", "終了月");

	private final MessageSource messageSource;

	public RowErrorMessages(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @param sectionNames リスト名 → 見出し（例: koteiList → 固定費）。1つしかリストがない画面は空でよい。
	 */
	public List<String> build(BindingResult result, Map<String, String> sectionNames) {
		List<String> messages = new ArrayList<>();
		for (ObjectError error : result.getAllErrors()) {
			if (error instanceof FieldError fe) {
				Matcher m = ROW_FIELD.matcher(fe.getField());
				if (m.matches()) {
					String section = sectionNames.getOrDefault(m.group(1), "");
					int rowNo = Integer.parseInt(m.group(2)) + 1;
					String prefix = (section.isEmpty() ? "" : section + " ") + rowNo + "行目:";
					messages.add(prefix + fieldMessage(fe, m.group(3)));
					continue;
				}
			}
			messages.add(message(error.getCode(), error.getArguments()));
		}
		return messages.stream().distinct().toList();
	}

	private String fieldMessage(FieldError fe, String field) {
		String label = LABELS.getOrDefault(field, field);
		String code = fe.getCode();
		return switch (code == null ? "" : code) {
		case "NotNull", "NotBlank" -> message("E001", label);
		case "Min" -> message("E002", label);
		case "Size" -> message("E003", label);
		case "Pattern" -> message("E004", label);
		case "typeMismatch" -> "kingaku".equals(field) ? message("E002", label) : message("E001", label);
		default -> message(code, fe.getArguments());
		};
	}

	private String message(String code, Object... args) {
		return messageSource.getMessage(code, args, code, Locale.JAPAN);
	}
}
