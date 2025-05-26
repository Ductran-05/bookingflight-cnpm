package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.request.ChangePasswordRequest;
import com.cnpm.bookingflight.dto.request.RegisterRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {

    private final EmailService emailService;

    private final VerificationTokenRepository verificationTokenRepository;

    final PasswordEncoder passwordEncoder;
    final AccountRepository accountRepository;
    final AccountMapper accountMapper;
    final ImageUploadService imageUploadService;

    public ResponseEntity<APIResponse<List<AccountResponse>>> getAllAccounts() {
        APIResponse<List<AccountResponse>> response = APIResponse.<List<AccountResponse>>builder()
                .status(200)
                .message("Get all accounts successfully")
                .data(accountRepository.findAllByIsDeletedFalse().stream().map(accountMapper::toAccountResponse)
                        .toList())
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> getAccountById(Long id) {
        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(200)
                .message("Get account by id successfully")
                .data(accountMapper.toAccountResponse(
                        accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> createAccount(AccountRequest request, MultipartFile avatar)
            throws IOException {
        Account account = accountRepository.findByUsername(request.getUsername()).orElse(null);
        if (account != null) {
            throw new AppException(ErrorCode.EXISTED);
        }

        Account newAccount = accountMapper.toAccount(request);
        newAccount.setIsDeleted(false); // Đảm bảo isDeleted là false khi tạo mới
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            newAccount.setAvatar(avatarUrl);
        }

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(201)
                .message("Create account successfully")
                .data(accountMapper.toAccountResponse(accountRepository.save(newAccount)))
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> updateAccount(Long id, AccountRequest request,
            MultipartFile avatar)
            throws IOException {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Account updatedAccount = accountMapper.toAccount(request);
        updatedAccount.setId(id);
        updatedAccount.setPassword(existingAccount.getPassword()); // Giữ nguyên password
        updatedAccount.setIsDeleted(existingAccount.getIsDeleted()); // Giữ nguyên trạng thái isDeleted
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            updatedAccount.setAvatar(avatarUrl);
        } else {
            updatedAccount.setAvatar(existingAccount.getAvatar());
        }

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(200)
                .message("Update account successfully")
                .data(accountMapper.toAccountResponse(accountRepository.save(updatedAccount)))
                .build();
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<APIResponse<Void>> deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        existingAccount.setIsDeleted(true); // Chuyển sang trạng thái xóa mềm
        accountRepository.save(existingAccount);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Delete account successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> changePassword(Long id, ChangePasswordRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Change password successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<AccountResponse>> registerUser(RegisterRequest request) {
        // Kiểm tra email đã tạo tài khoản thành công hay chua
        Account existingAccount = accountRepository.findByUsername(request.getUsername()).orElse(null);
        if (existingAccount != null) {
            if (existingAccount.getEnabled() == false) {
                deleteAccount(existingAccount.getId());
            } else {
                throw new AppException(ErrorCode.EXISTED);
            }
        }
        // Tạo account chờ kích hoạt
        Account account = Account.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .username(request.getUsername())
                .password(request.getPassword())
                .isDeleted(false) // Đảm bảo isDeleted là false khi đăng ký
                .build();
        accountRepository.save(account);

        // Tạo token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .account(account)
                .expiryDate(LocalDateTime.now().plusDays(01))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Gửi mail
        String link = "http://localhost:8080/auth/confirm?token=" + token;
        System.out.println(link);
        emailService.send(account.getUsername(), buildEmail(link));

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(201)
                .message("Create account successfully")
                .data(accountMapper.toAccountResponse(account))
                .build();
        return ResponseEntity.ok(response);
    }

    private String buildEmail(String link) {
        return "Chào bạn,\n\n"
                + "Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào liên kết dưới đây để xác thực email:\n"
                + link + "\n\n"
                + "Liên kết này sẽ hết hạn sau 24 giờ.\n\n"
                + "Trân trọng.";
    }

    public void updateAccountRefreshToken(String refreshToken, String username) {
        Account currAccount = accountRepository.findByUsername(username).orElse(null);
        if (currAccount != null) {
            currAccount.setRefreshToken(refreshToken);
            accountRepository.save(currAccount);
        }
    }

    public Account findByUsernameAndRefreshToken(String username, String refreshToken) {
        return accountRepository.findByUsernameAndRefreshToken(username, refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }
}