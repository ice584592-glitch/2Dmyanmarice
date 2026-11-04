# Myanmar 2D/3D Android app

ဤ repository တွင် screenshot ပုံစံကို အခြေခံထားသော Kotlin + Jetpack Compose Android app နှင့် API key ကို Android app ထဲမထည့်ဘဲ ��ားနိုင်ရန် proxy server ပါဝင်သည်။

## အရေးကြီး — API key လုံခြုံရေး

`SETLIVE_API_KEY` ကို Android code, GitHub, သို့မဟုတ် APK ထဲ မထည့်ပါနှင့်။ Backend server ပေါ်တွင် environment variable အဖြစ် သတ်မှတ်ပါ။ Proxy ကသာ upstream API ကို API key ဖြင့် ခေါ်ပေးသည်။

## Backend ကို စတင်ရန်

Node.js 18+ လိုအပ်သည် (built-in `fetch` အသုံးပြုထားသည်)။

```bash
cd backend
npm install
SETLIVE_API_KEY='သင့်_API_KEY' npm start
```

Server ပေါ်တွင် `SETLIVE_API_KEY` ကို secret/environment variable အဖြစ် ထည့်ပါ။ Public internet သို့တင်မည်ဆိုလျှင် HTTPS သုံးပါ။ Production အသုံးပြုမှုမတိုင်မီ rate limit နှင့် app/backend access control ထည့်သင့်သည်။

## Android app ချိတ်ရန်

Android Studio မှ project root-ийг нээж Gradle sync хийнэ.

- Android Emulator дотор локал backend ашиглах default URL: `http://10.0.2.2:3000/`
- Бодит утас эсвэл байршуулсан сервер ашиглах бол хэрэглэгчийн `~/.gradle/gradle.properties` файлд дараах мөрийг нэмнэ:

```properties
MYANMAR_API_BASE_URL=https://your-backend.example.com/
```

Нөгөө утаснаасаа локал компьютерийн backend рүү холбогдох бол `10.0.2.2` бус компьютерийн дотоод IP ашиглана. HTTPS ашиглахыг зөвлөж байна.

## Одоогоор багтсан

- Live 2D үндсэн дэлгэц, 2D түүх, 3D сүүлийн үр дүнгийн tab
- Таны өгсөн API response-ийн талбаруудыг харуулах
- Дахин ачаалах товч; tab солих бүрт API-аас шинэ өгөгдөл авах
- API key-г аппликэйшнээс тусад нь хадгалах Node.js proxy
- API уналаа/сүлжээ тасарлаа үед алдааны мессеж

2D ticks болон 3D history endpoint клиентэд бэлэн; дараагийн хувилбарт тусдаа жагсаалтын дэлгэц нэмж болно. SSE stream-ийг backend proxy дамжуулдаг. Энэ эхний хувилбар live result-ийг refresh хийх байдлаар авна.
