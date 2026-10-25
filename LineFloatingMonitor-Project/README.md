# LINE 群組即時懸浮窗 (LineFloatingMonitor)

這是一個 Android 專案，能夠攔截 LINE 群組通知並顯示在桌面的可移動、可縮放浮動視窗上。

## 如何免安裝 Android Studio 取得 APK (GitHub Actions 雲端打包)
1. 將此壓縮檔解壓縮。
2. 在 GitHub 建立一個新的 Repository（儲存庫）。
3. 點擊 **Add file** -> **Upload files**，將解壓縮後的所有檔案與資料夾（包含 `.github`、`app`、`build.gradle`、`settings.gradle`、`gradle.properties`）拖曳上傳並按下 **Commit changes**。
   *(注意：`.github` 資料夾在部分電腦可能預設為隱藏檔，請確認有一起拖曳上傳，或直接在 GitHub 網頁點選 Create new file 輸入 `.github/workflows/build.yml` 貼上內容)*
4. 點擊上方 **Actions** 標籤，等待約 2 分鐘編譯完成（綠色勾勾）。
5. 點進編譯紀錄最下方的 **Artifacts**，即可下載 `LineFloatingMonitor-APK`！
