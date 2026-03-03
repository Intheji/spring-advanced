package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니당")
    @Pattern(regexp = ".*\\d.*", message = "새 비밀번호에는 숫자가 포함되어야 합니당")
    @Pattern(regexp = ".*[A-Z].*", message = "새 비밀번호에는 대문자가 포함되어야 합니당")
    private String newPassword;

}
