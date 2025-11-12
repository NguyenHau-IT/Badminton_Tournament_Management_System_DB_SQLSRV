# 🧪 Quick Test: Icons Migration

## Run these commands to test:

```powershell
# 1. Build
cd "c:\Users\HUNG\OneDrive\Desktop\Badminton_Tournament_Management_System_DB_SQLSRV"
mvn clean package -DskipTests

# 2. Run
java -jar target/btms-2.0.0.jar --server.port=2345

# 3. Open browser
# http://localhost:2345
```

## ✅ What to check:

### Tournament Preview Section:
- [ ] 4 tournament cards hiển thị đầy đủ
- [ ] Hình ảnh SVG load thành công (không có broken images)
- [ ] Hover effects hoạt động

### DevTools → Network tab:
- [ ] Tìm các request đến `/icons/tournaments/*.svg`
- [ ] Tất cả status là 200 (không có 404)
- [ ] Content-Type: `image/svg+xml`

### DevTools → Console:
- [ ] Không có lỗi 404 Not Found
- [ ] Không có ERR_NAME_NOT_RESOLVED
- [ ] Console clean (không có errors màu đỏ)

## ❌ If you see errors:

### Error: 404 for icons/tournaments/*.svg
**Cause:** Maven chưa copy icons vào target/
**Fix:** 
```powershell
# Force rebuild
mvn clean package -DskipTests -U
```

### Error: Broken images
**Cause:** Path trong JSON sai
**Fix:** Check JSON paths:
```powershell
Get-Content src\main\resources\data\tournaments.json | Select-String '"image":'
# Should all be: "/icons/tournaments/*.svg"
```

---

## ✅ Success Criteria:
All tournament images load from `/icons/tournaments/` with status 200 and no console errors.
