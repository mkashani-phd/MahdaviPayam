#  <img src="img/Mahdavi-payam.png" alt="Mahdavi Payam logo" width="60" style="vertical-align: middle;" /> Mahdavi Payam (مهدوی پیام)


---

<div dir="rtl">

## راهنمای فارسی (Persian Guide)

**مهدوی پیام** یک اپلیکیشن اندرویدی با امنیت بالا است که برای تایید اصالت پیام‌های امضا شده از طریق کدهای QR طراحی شده است. هدف اصلی این برنامه اطمینان از این است که اطلاعات دریافتی کاربران واقعی، بدون تغییر و از منبعی مورد اعتماد ارسال شده است.

> 💡 **Powered by:** [freeiranprotests.com](https://freeiranprotests.com)

### 📱 بررسی کلی
این اپلیکیشن به عنوان یک اسکنر QR تخصصی عمل می‌کند. به جای خواندن متن ساده، یک ساختار JSON امن شامل پیام و امضای دیجیتال مربوطه را تجزیه و تحلیل می‌کند.

*   **⚡ اسکن خودکار**: فعال‌سازی فوری دوربین بلافاصله پس از اجرا.
*   **🖼️ پشتیبانی از گالری**: امکان انتخاب تصاویر کد QR از حافظه گوشی.
*   **🎨 رابط کاربری بومی**: محیط کاملاً فارسی و کاربرپسند.
*   **🔔 تایید لحظه‌ای**: بازخورد بصری فوری (✅ تایید شد یا ❌ خطا) از طریق Dialog.

### 🔒 جزئیات امنیتی
مهدوی پیام از یک معماری چندلایه برای محافظت از یکپارچگی داده‌ها استفاده می‌کند:

1.  **حفاظت در برابر دستکاری (Anti-Tampering):**
    *   **گواهی Certificate Pinning**: هش SHA-256 گواهی رسمی در کد برنامه Hard-code شده است.
    *   **تایید زمان اجرا**: اگر برنامه اصلاح شود، سیستم آن را مسدود کرده و پیام خطا نمایش می‌دهد.
2.  **مدیریت امن کلیدها:**
    *   استفاده از `EncryptedSharedPreferences` با رمزنگاری **AES256**.
    *   پشتیبانی از **StrongBox** (ماژول امنیت سخت‌افزاری) برای جلوگیری از استخراج کلید.
3.  **موتور رمزنگاری:**
    *   استفاده از الگوریتم مدرن **Ed25519** (از طریق کتابخانه Google Tink).


**راهنمای تأیید QR کد کلید عمومی امضاشده**

برای بررسی صحت این QR کد، لطفاً از **همین اپلیکیشن** استفاده کنید.

1. اپلیکیشن را باز کنید.
2. وارد بخش **Scan** یا **Verify** شوید.
3. همین QR کد را اسکن کنید.
4. نتیجه باید وضعیت **Verified / Valid Signature** را نمایش دهد.

<p align="center">
  <img src="img/signed_public_key.png" alt="Signed public key QR code" width="200" />
</p>


---
</div>

## 🇬🇧 English Guide

**Mahdavi Payam** is a high-security Android application designed to verify the authenticity of signed messages delivered via QR codes. It ensures that information received is genuine, untampered, and originated from a trusted source.

> 💡 **Powered by:** [freeiranprotests.com](https://freeiranprotests.com)

### 📱 General Overview
The app functions as a specialized QR scanner that parses a secure JSON structure containing a message and its corresponding digital signature.

*   **⚡ Automatic Scanning**: Launches the camera immediately upon startup.
*   **🖼️ Gallery Support**: Allows users to import and scan QR code images from the gallery.
*   **🔔 Instant Verification**: Provides immediate feedback via a Farsi pop-up dialog.

### 🔒 Security Implementation Details

#### 1. App Integrity Protection
*   **Certificate Pinning**: Stores a SHA-256 hash of the official signing certificate within the binary.
*   **Runtime Verification**: If the app is modified or resigned by an attacker, it automatically blocks usage to prevent phishing.

#### 2. Secure Key Management
*   **Hardware-Backed Storage**: Utilizes `EncryptedSharedPreferences`. On supported devices, it leverages **StrongBox Keymaster** to ensure keys never leave the hardware security module.

#### 3. Cryptographic Verification Engine
*   **Algorithm**: Employs **Ed25519** (via **Google Tink**), offering high performance and high security.
*   **Process**: Validates the digital signature against the pre-provisioned root public key.

### 📑 Secure QR Data Format
The application expects a JSON structure with the following fields:

| Field | Purpose | Technical Detail |
| :--- | :--- | :--- |
| `k` | **Key Alias** | Identifies the signing key (e.g., "android") |
| `m` | **Message** | The actual content/text to be displayed |
| `s` | **Signature** | Ed25519 signature encoded in URL-safe Base64 |

---

### 🛠 Tech Stack
*   **Language**: Kotlin/Java (Android)
*   **Crypto Library**: Google Tink
*   **Scanning Engine**: ZXing / ML Kit
