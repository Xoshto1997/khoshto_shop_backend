package com.example.ecommerce_project.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    public String generateSecretKey() {
        return new DefaultSecretGenerator().generate();
    }

    public String generateQrCodeImageUri(String secret, String email) {
        QrData data = new QrData.Builder()
                .label(email)
                .issuer("3D-Studio")
                .secret(secret)
                .digits(6)
                .period(30)
                .build();

        ZxingPngQrGenerator generator = new ZxingPngQrGenerator();
        try {
            byte[] imageData = generator.generate(data);
            return Utils.getDataUriForImage(imageData, "image/png");
        } catch (Exception e) {
            throw new RuntimeException("QR კოდის გენერაციის შეცდომა", e);
        }
    }

    public boolean isCodeValid(String secret, String code) {
        if (secret == null || code == null) {
            System.out.println("=== MFA DEBUG: Secret ან Code არის NULL ===");
            return false;
        }



        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        boolean isValid = verifier.isValidCode(secret, code);

        if (!isValid) {
            long currentTime = timeProvider.getTime();

            TimeProvider pastTimeProvider = () -> currentTime - 30;
            CodeVerifier pastVerifier = new DefaultCodeVerifier(codeGenerator, pastTimeProvider);

            TimeProvider futureTimeProvider = () -> currentTime + 30;
            CodeVerifier futureVerifier = new DefaultCodeVerifier(codeGenerator, futureTimeProvider);

            isValid = pastVerifier.isValidCode(secret, code) || futureVerifier.isValidCode(secret, code);
        }


        return isValid;
    }
}