package com.cnpm.bookingflight.service;

import com.cnpm.bookingflight.domain.Account;
import com.cnpm.bookingflight.domain.VerificationToken;
import com.cnpm.bookingflight.dto.ResultPaginationDTO;
import com.cnpm.bookingflight.dto.request.AccountRequest;
import com.cnpm.bookingflight.dto.request.ChangePasswordRequest;
import com.cnpm.bookingflight.dto.request.RegisterRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.dto.response.AccountResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.AccountMapper;
import com.cnpm.bookingflight.mapper.ResultPaginationMapper;
import com.cnpm.bookingflight.repository.AccountRepository;
import com.cnpm.bookingflight.repository.RoleRepository;
import com.cnpm.bookingflight.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    final ResultPaginationMapper resultPaginationMapper;
    final RoleRepository roleRepository;

    public ResponseEntity<APIResponse<ResultPaginationDTO>> getAllAccounts(Specification<Account> spec,
                                                                           Pageable pageable) {
        spec = spec.and((root, query, cb) -> cb.equal(root.get("isDeleted"), false));
        Page<AccountResponse> page = accountRepository.findAll(spec, pageable).map(accountMapper::toAccountResponse);
        ResultPaginationDTO resultPaginationDTO = resultPaginationMapper.toResultPagination(page);
        APIResponse<ResultPaginationDTO> response = APIResponse.<ResultPaginationDTO>builder()
                .status(200)
                .message("Get all accounts successfully")
                .data(resultPaginationDTO)
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
        newAccount.setEnabled(true);
        newAccount.setIsDeleted(false);
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
                                                                      MultipartFile avatar) throws IOException {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Account updatedAccount = accountMapper.updateAccount(request, existingAccount);
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = imageUploadService.uploadImage(avatar, "avatars");
            updatedAccount.setAvatar(avatarUrl);
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
        existingAccount.setIsDeleted(true);
        accountRepository.save(existingAccount);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Delete account successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<APIResponse<Void>> hardDeleteAccount(Long id) {
        accountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        verificationTokenRepository.deleteByAccountId(id);
        accountRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(200)
                .message("Hard delete account successfully")
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
        Account existingAccount = accountRepository.findByUsernameAndIsDeletedFalse(request.getUsername()).orElse(null);
        if (existingAccount != null) {
            if (!existingAccount.getEnabled()) {
                hardDeleteAccount(existingAccount.getId());
            } else {
                throw new AppException(ErrorCode.EXISTED);
            }
        }

        Account account = Account.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .isDeleted(false)
                .role(roleRepository.findByRoleName("USER").orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)))
                .build();
        accountRepository.save(account);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .account(account)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();
        verificationTokenRepository.save(verificationToken);

        String link = "http://localhost:8080/auth/confirm?token=" + token;
        emailService.send(account.getUsername(), buildEmail(link));

        APIResponse<AccountResponse> response = APIResponse.<AccountResponse>builder()
                .status(201)
                .message("Create account successfully")
                .data(accountMapper.toAccountResponse(account))
                .build();
        return ResponseEntity.ok(response);
    }

    private String buildEmail(String link) {
        return "Chào bạn,\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào liên kết dưới đây để xác thực email:\n" +
                link + "\n\n" +
                "Liên kết này sẽ hết hạn sau 24 giờ.\n\n" +
                "Trân trọng.";
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