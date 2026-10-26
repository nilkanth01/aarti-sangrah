/* देवघर (Devghar) — web / iPhone version of the Android app. No libraries, no tracking.
 * Content comes from content.json (the same file the Android app uses). Favourites and
 * settings stay in this browser's localStorage. */
'use strict';

(() => {
  const REPO = 'https://github.com/nilkanth01/aarti-sangrah';

  // ---------- Storage (never required to work) ----------
  const store = {
    get(key, fallback) {
      try {
        const v = localStorage.getItem('aarti.' + key);
        return v === null ? fallback : JSON.parse(v);
      } catch { return fallback; }
    },
    set(key, value) {
      try { localStorage.setItem('aarti.' + key, JSON.stringify(value)); } catch { /* private mode */ }
    },
  };

  const state = {
    lang: store.get('lang', 'mr'),
    theme: store.get('theme', 'auto'),
    fontSize: store.get('fontSize', 22),
    keepAwake: store.get('keepAwake', true),
    speechSlow: store.get('speechSlow', false),
    music: store.get('music', true),
    favorites: store.get('favorites', []),
    recents: store.get('recents', []),
  };
  let lib = null;
  const searchState = { q: '', cat: null };

  // ---------- Marathi / English strings ----------
  const DIGITS = '०१२३४५६७८९';
  const mrNum = n => String(n).replace(/[0-9]/g, d => DIGITS[d]);
  const MR_DAYS = ['रविवार', 'सोमवार', 'मंगळवार', 'बुधवार', 'गुरुवार', 'शुक्रवार', 'शनिवार'];
  const EN_DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const MR_MONTHS = ['जानेवारी', 'फेब्रुवारी', 'मार्च', 'एप्रिल', 'मे', 'जून', 'जुलै', 'ऑगस्ट', 'सप्टेंबर', 'ऑक्टोबर', 'नोव्हेंबर', 'डिसेंबर'];
  const EN_MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  /** The deity traditionally worshipped on each weekday (Sunday first). */
  const DAY_DEITY = ['surya', 'shankar', 'ganpati', 'vitthal', 'datta', 'devi', 'maruti'];

  const STR = {
    mr: {
      tabHome: 'मुख्य', tabSearch: 'शोधा', tabFav: 'आवडते', tabSettings: 'सेटिंग्ज', back: 'मागे',
      appName: 'देवघर', tagline: 'आरत्या • श्लोक • स्तोत्रे • अभंग', searchHint: 'आरती, श्लोक किंवा देव शोधा…',
      otherLang: 'English', sequences: 'पूजा क्रम', categories: 'प्रकार', deities: 'देवता', recent: 'अलीकडे वाचलेले',
      start: 'सुरू करा', all: 'सर्व', footer: 'खाते नाही • जाहिराती नाहीत • इंटरनेटशिवायही चालते',
      today: d => `आज ${MR_DAYS[d.getDay()]}`, deityOfDay: n => `आजचे दैवत: ${n}`,
      parts: n => `${mrNum(n)} भाग`, texts: n => `${mrNum(n)} रचना`,
      textSize: 'अक्षरांचा आकार', share: 'शेअर करा', prev: 'मागील', next: 'पुढील',
      addFav: 'आवडत्यांत जोडा', removeFav: 'आवडत्यांतून काढा', shareFooter: '— देवघर', by: a => `रचना: ${a}`,
      searchSub: 'मराठी किंवा इंग्रजीत शोधा — उदा. सुखकर्ता, ganpati, ramraksha', searchPh: 'आरती, श्लोक, देव किंवा ओळ…',
      clear: 'पुसा', results: 'शोध परिणाम', noResults: 'काही सापडले नाही',
      noResultsHint: 'दुसरा शब्द वापरून पहा. इंग्रजी अक्षरांतही शोधता येते — उदा. aarti, vitthal, datta.',
      favEmptySub: 'तुमच्या आवडत्या आरत्या एका ठिकाणी', favCount: n => `${mrNum(n)} आवडत्या रचना`,
      favEmptyTitle: 'अजून काही आवडते नाही', favEmptyMsg: 'कोणतीही आरती उघडा आणि ♡ दाबा — ती येथे लगेच मिळेल.',
      settingsSub: 'तुमच्या सोयीनुसार ॲप बदला', langGroup: 'भाषा • Language', appLang: 'ॲपची भाषा',
      langNote: 'मेनू व माहिती या भाषेत दिसेल. आरत्या, श्लोक व स्तोत्रे नेहमी देवनागरीतच राहतील.',
      appearance: 'दिसणे', theme: 'रंगसंगती', auto: 'आपोआप', day: 'दिवस', night: 'रात्र',
      keepAwake: 'वाचताना स्क्रीन चालू ठेवा', keepAwakeHint: 'आरती म्हणताना स्क्रीन बंद होणार नाही',
      keepAwakeNo: 'हा ब्राउझर ही सुविधा देत नाही.',
      updates: 'अपडेट्स', collection: 'आरत्या व श्लोक', collectionInfo: (v, n) => `आवृत्ती ${mrNum(v)} • ${mrNum(n)} रचना`,
      checkNew: 'नवीन तपासा', otaNote: 'नवीन आरत्या इंटरनेट असताना आपोआप येतात.',
      about: 'माहिती',
      aboutText: 'हे ॲप पूर्णपणे मोफत आहे — खाते नाही, जाहिराती नाहीत, तुमची कोणतीही माहिती गोळा केली जात नाही. एकदा उघडल्यावर इंटरनेटशिवायही चालते.',
      report: 'चूक कळवा / नवीन आरती सुचवा', source: 'स्रोत कोड (GitHub)', android: 'Android ॲप (APK) डाउनलोड',
      installTitle: 'फोनवर ॲप म्हणून ठेवा',
      installIOS: (s, a) => `Safari मध्ये ${s} Share दाबा, मग ${a} “Add to Home Screen” निवडा.`,
      installAndroid: 'होम स्क्रीनवर आयकॉन ठेवा — इंटरनेटशिवायही चालेल.', install: 'इंस्टॉल', dismiss: 'बंद करा',
      copied: 'मजकूर कॉपी केला', appUpdated: 'ॲपची नवीन आवृत्ती आली', refresh: 'रीफ्रेश',
      upToDate: 'सर्व आरत्या अद्ययावत आहेत 🙏', offline: 'इंटरनेट जोडणी तपासा आणि पुन्हा प्रयत्न करा',
      contentUpdated: n => n > 0 ? `${mrNum(n)} नवीन आरत्या/श्लोक जोडले गेले 🌼` : 'आरत्या व श्लोक अद्ययावत झाले 🌼',
      loadError: 'आरत्या लोड झाल्या नाहीत. इंटरनेट तपासून पुन्हा प्रयत्न करा.', retry: 'पुन्हा प्रयत्न करा',
      listen: 'ऐका', pause: 'थांबवा', resume: 'पुढे ऐका', stopListening: 'बंद करा', slow: 'हळू', normalSpeed: 'सामान्य',
      music: 'तंबोरा', lineOf: (i, n) => `ओळ ${mrNum(i)} / ${mrNum(n)}`,
      hindiVoiceNote: 'मराठी आवाज नाही — हिंदी आवाज वापरत आहे',
      noVoice: 'या फोनवर मराठी किंवा हिंदी आवाज नाही.', noSpeech: 'हा ब्राउझर वाचून दाखवू शकत नाही.',
      listenGroup: 'ऐका', speechSpeed: 'वाचण्याचा वेग', musicSetting: 'सोबत तंबोरा वाजवा',
      listenNote: '“ऐका” दाबल्यावर आरती ओळ-ओळ वाचली जाते आणि चालू ओळ रंगीत दिसते — उच्चार शिकण्यासाठी. आवाज फोन/ब्राउझरचा स्वतःचा असतो.',
      voiceStatus: v => (v === 'mr' ? 'आवाज: मराठी ✓' : v === 'hi' ? 'मराठी आवाज नाही — हिंदी आवाज वापरला जाईल' : 'या ब्राउझरमध्ये मराठी/हिंदी आवाज सापडला नाही'),
    },
    en: {
      tabHome: 'Home', tabSearch: 'Search', tabFav: 'Favourites', tabSettings: 'Settings', back: 'Back',
      appName: 'Devghar', tagline: 'Aartis • Shlokas • Stotras • Abhangs', searchHint: 'Search aartis, shlokas or deities…',
      otherLang: 'मराठी', sequences: 'Pooja sequences', categories: 'Categories', deities: 'Deities', recent: 'Recently read',
      start: 'Start', all: 'All', footer: 'No account • No ads • Works offline',
      today: d => `Today is ${EN_DAYS[d.getDay()]}`, deityOfDay: n => `Deity of the day: ${n}`,
      parts: n => (n === 1 ? '1 part' : `${n} parts`), texts: n => (n === 1 ? '1 text' : `${n} texts`),
      textSize: 'Text size', share: 'Share', prev: 'Previous', next: 'Next',
      addFav: 'Add to favourites', removeFav: 'Remove from favourites', shareFooter: '— Devghar', by: a => `By ${a}`,
      searchSub: 'Type in English or Marathi — e.g. ganpati, ramraksha, सुखकर्ता', searchPh: 'Aarti, shloka, deity or a line…',
      clear: 'Clear', results: 'Search results', noResults: 'Nothing found',
      noResultsHint: 'Try another word. English spellings work too — e.g. aarti, vitthal, datta.',
      favEmptySub: 'Your favourite aartis in one place', favCount: n => (n === 1 ? '1 favourite' : `${n} favourites`),
      favEmptyTitle: 'No favourites yet', favEmptyMsg: 'Open any aarti and tap ♡ — it will show up here.',
      settingsSub: 'Make the app your own', langGroup: 'Language • भाषा', appLang: 'App language',
      langNote: 'Menus and notes use this language. Aartis, shlokas and stotras always stay in Devanagari.',
      appearance: 'Appearance', theme: 'Theme', auto: 'Auto', day: 'Day', night: 'Night',
      keepAwake: 'Keep screen on while reading', keepAwakeHint: "The screen won't sleep during an aarti",
      keepAwakeNo: "This browser doesn't support it.",
      updates: 'Updates', collection: 'Aartis & shlokas', collectionInfo: (v, n) => `Version ${v} • ${n} texts`,
      checkNew: 'Check for new', otaNote: 'New aartis arrive automatically whenever you are online.',
      about: 'About',
      aboutText: 'This app is completely free — no account, no ads, and none of your data is collected. Once opened, it works without internet too.',
      report: 'Report a mistake / suggest an aarti', source: 'Source code (GitHub)', android: 'Download the Android app (APK)',
      installTitle: 'Keep it on your phone as an app',
      installIOS: (s, a) => `In Safari, tap ${s} Share, then ${a} “Add to Home Screen”.`,
      installAndroid: 'Add an icon to your home screen — it works offline too.', install: 'Install', dismiss: 'Close',
      copied: 'Text copied', appUpdated: 'A new version of the app is ready', refresh: 'Refresh',
      upToDate: 'All aartis are up to date 🙏', offline: 'Check your internet connection and try again',
      contentUpdated: n => (n > 0 ? `${n} new aartis/shlokas added 🌼` : 'Aartis & shlokas updated 🌼'),
      loadError: "Couldn't load the aartis. Check your internet and try again.", retry: 'Try again',
      listen: 'Listen', pause: 'Pause', resume: 'Resume', stopListening: 'Stop', slow: 'Slow', normalSpeed: 'Normal',
      music: 'Tanpura', lineOf: (i, n) => `Line ${i} of ${n}`,
      hindiVoiceNote: 'No Marathi voice — using the Hindi voice',
      noVoice: 'This device has no Marathi or Hindi voice.', noSpeech: "This browser can't read text aloud.",
      listenGroup: 'Listen', speechSpeed: 'Reading speed', musicSetting: 'Play tanpura in the background',
      listenNote: '“Listen” reads the text line by line and highlights the current line, to help with pronunciation. The voice is your phone’s or browser’s own.',
      voiceStatus: v => (v === 'mr' ? 'Voice: Marathi ✓' : v === 'hi' ? 'No Marathi voice — the Hindi voice will be used' : 'No Marathi or Hindi voice found in this browser'),
    },
  };
  const S = () => STR[state.lang];
  const en = () => state.lang === 'en';
  const num = n => (en() ? String(n) : mrNum(n));

  const catName = c => (en() ? c.nameEn || c.name : c.name);
  const catDesc = c => (en() ? c.descriptionEn || c.description : c.description);
  const deityName = d => (en() ? d.nameEn || d.name : d.name);
  const deityShort = d => (en() ? deityName(d) : d.name.replace(/^श्री /, '').replace(/^सद्गुरु /, ''));
  const plName = p => (en() ? p.nameEn || p.name : p.name);
  const plDesc = p => (en() ? p.descriptionEn || p.description : p.description);
  const aboutOf = i => (en() ? i.aboutEn || i.about : i.about);
  const LANG_EN = { 'मराठी': 'Marathi', 'संस्कृत': 'Sanskrit', 'हिंदी': 'Hindi', 'अवधी': 'Awadhi' };
  const langName = l => (en() ? LANG_EN[l] || l : l);
  const dateText = (d = new Date()) => (en()
    ? `${EN_DAYS[d.getDay()]}, ${d.getDate()} ${EN_MONTHS[d.getMonth()]}`
    : `${MR_DAYS[d.getDay()]}, ${mrNum(d.getDate())} ${MR_MONTHS[d.getMonth()]}`);

  // ---------- Icons (Material Icons, Apache-2.0, plus a few drawn for this app) ----------
  const ICONS = {
    home: '<path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>',
    search: '<path d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>',
    heart: '<path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>',
    'heart-o': '<path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/>',
    settings: '<path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.49.49 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.48.48 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a.48.48 0 0 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.49.49 0 0 0-.12-.61l-2.01-1.58zM12 15.6A3.61 3.61 0 0 1 8.4 12c0-1.98 1.62-3.6 3.6-3.6s3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>',
    back: '<path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>',
    share: '<path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11A2.99 2.99 0 0 0 21 5c0-1.66-1.34-3-3-3s-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81A2.99 2.99 0 0 0 3 12a2.99 2.99 0 0 0 5.04 2.19l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/>',
    size: '<path d="M9 4v3h5v12h3V7h5V4H9zm-6 8h3v7h3v-7h3V9H3v3z"/>',
    'chev-left': '<path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>',
    'chev-right': '<path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>',
    close: '<path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>',
    play: '<path d="M8 5v14l11-7z"/>',
    info: '<path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>',
    note: '<path d="M12 3v10.55A4 4 0 1 0 14 17V7h4V3h-6z"/>',
    queue: '<path d="M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18A3 3 0 1 0 19 17V8h3V6h-5z"/>',
    flame: '<path fill-rule="evenodd" d="M12 2.5c2.2 3 5 5.6 5 9.1a5 5 0 0 1-10 0c0-3.5 2.8-6.1 5-9.1zm0 5.6c-1 1.4-2.2 2.6-2.2 4.1a2.2 2.2 0 0 0 4.4 0c0-1.5-1.2-2.7-2.2-4.1z"/><path d="M3.5 17.5h17c-.7 2.4-4.2 4-8.5 4s-7.8-1.6-8.5-4z"/>',
    book: '<path d="M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z"/>',
    translate: '<path d="M12.87 15.07l-2.54-2.51.03-.03A17.52 17.52 0 0 0 14.07 6H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/>',
    sun: '<circle cx="12" cy="12" r="4.5"/><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" d="M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M4.9 19.1l1.8-1.8M17.3 6.7l1.8-1.8"/>',
    moon: '<path d="M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.39 5.39 0 0 1-4.4 2.26 5.4 5.4 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/>',
    auto: '<path d="M12 4a8 8 0 0 0 0 16V4z"/><circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" stroke-width="2"/>',
    download: '<path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>',
    'ios-share': '<path d="M16 5l-1.42 1.42-1.59-1.59V16h-1.98V4.83L9.42 6.42 8 5l4-4 4 4zm4 5v11c0 1.1-.9 2-2 2H6a2 2 0 0 1-2-2V10c0-1.11.89-2 2-2h3v2H6v11h12V10h-3V8h3c1.1 0 2 .89 2 2z"/>',
    'add-box': '<path d="M19 3H5a2 2 0 0 0-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V7h2v4h4v2z"/>',
    open: '<path d="M19 19H5V5h7V3H5a2 2 0 0 0-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"/>',
    feedback: '<path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/>',
    code: '<path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>',
    check: '<path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>',
    headphones: '<path d="M12 1a9 9 0 0 0-9 9v7c0 1.66 1.34 3 3 3h3v-8H5v-2c0-3.87 3.13-7 7-7s7 3.13 7 7v2h-4v8h3c1.66 0 3-1.34 3-3v-7a9 9 0 0 0-9-9z"/>',
    pause: '<path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>',
    'music-off': '<path d="M4.27 3L3 4.27l9 9v.28A4 4 0 1 0 14 17v-1.73L19.73 21 21 19.73 4.27 3zM14 7h4V3h-6v5.18l2 2z"/>',
  };
  const icon = (name, cls = '') => `<svg class="ic ${cls}" viewBox="0 0 24 24" aria-hidden="true">${ICONS[name] || ''}</svg>`;
  const catIcon = id => (id === 'shlok'
    ? '<span class="glyph" aria-hidden="true">ॐ</span>'
    : icon({ aarti: 'flame', stotra: 'book', abhang: 'note' }[id] || 'book'));

  const ORNAMENT = '<svg class="ornament" viewBox="0 0 300 18" aria-hidden="true">'
    + '<rect x="66" y="8.4" width="68" height="1.2" fill="currentColor" opacity=".35"/>'
    + '<rect x="166" y="8.4" width="68" height="1.2" fill="currentColor" opacity=".35"/>'
    + '<circle cx="140" cy="9" r="2" fill="currentColor" opacity=".7"/><circle cx="160" cy="9" r="2" fill="currentColor" opacity=".7"/>'
    + '<path d="M150 3l6 6-6 6-6-6z" fill="currentColor"/></svg>';

  const RINGS = (() => {
    let s = '<svg class="hero-rings" viewBox="0 0 400 330" preserveAspectRatio="xMaxYMin slice" aria-hidden="true"><g fill="none" stroke="#fff" stroke-opacity=".09">';
    for (let i = 1; i <= 7; i++) s += `<circle cx="368" cy="40" r="${i * 34}"/>`;
    for (let i = 1; i <= 4; i++) s += `<circle cx="8" cy="337" r="${i * 30}"/>`;
    s += '</g><g fill="#fff" fill-opacity=".1">';
    for (let k = 0; k < 24; k++) {
      const a = (k * 15 * Math.PI) / 180;
      s += `<circle cx="${(368 + 136 * Math.cos(a)).toFixed(1)}" cy="${(40 + 136 * Math.sin(a)).toFixed(1)}" r="5"/>`;
    }
    return s + '</g></svg>';
  })();

  // ---------- Helpers ----------
  const $ = (sel, el = document) => el.querySelector(sel);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
  const clamp = (v, lo, hi) => Math.min(hi, Math.max(lo, v));
  const isStandalone = () => window.navigator.standalone === true || matchMedia('(display-mode: standalone)').matches;
  const isIOS = () => /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
  const readHref = (ids, i, title) =>
    `#/read?ids=${encodeURIComponent(ids.join(','))}&i=${i}&t=${encodeURIComponent(title || '')}`;
  const deityStyle = d => `--dc:${esc(d?.color || '#8C5A00')}`;
  const avatar = (d, size) => `<span class="avatar s${size}" style="${deityStyle(d)}" lang="mr">${esc(d?.letter || 'ॐ')}</span>`;

  // ---------- Content ----------
  function preview(item) {
    const lines = [...(item.refrainFirst && item.refrain ? [item.refrain] : []), ...item.verses]
      .flatMap(v => v.split('\n'))
      .map(l => l.replace(/^[।॥\s०-९]+|[।॥\s०-९]+$/g, ''))
      .filter(Boolean);
    const key = item.title.slice(0, 4);
    return lines.find(l => !l.startsWith(key)) || lines[0] || '';
  }

  function buildLibrary(data) {
    const deityMap = new Map(data.deities.map(d => [d.id, d]));
    const catMap = new Map(data.categories.map(c => [c.id, c]));
    const items = data.items.filter(i => i.id && i.title && Array.isArray(i.verses) && i.verses.length);
    if (!items.length) throw new Error('empty content');
    const byId = new Map(items.map(i => [i.id, i]));
    for (const i of items) {
      const d = deityMap.get(i.deity);
      const c = catMap.get(i.category);
      i._search = [i.title, i.titleEn, i.keywords, i.author, d?.name, d?.nameEn, d?.keywords, c?.name, c?.nameEn, i.refrain, ...i.verses]
        .filter(Boolean).join(' ').toLowerCase();
      i._preview = preview(i);
    }
    const playlists = (data.playlists || [])
      .map(p => ({ ...p, items: (p.items || []).filter(id => byId.has(id)) }))
      .filter(p => p.items.length);
    return { version: data.version, items, byId, deities: data.deities, deityMap, categories: data.categories, catMap, playlists };
  }

  function search(q, cat) {
    const tokens = q.toLowerCase().split(/[\s,.]+/).filter(Boolean);
    return lib.items.filter(i => (!cat || i.category === cat) && tokens.every(t => i._search.includes(t)));
  }

  async function fetchContent(fresh) {
    const res = await fetch(fresh ? `content.json?fresh=${Date.now()}` : 'content.json', { cache: 'no-cache' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return buildLibrary(await res.json());
  }

  // ---------- Settings that touch the page ----------
  function applyTheme() {
    const dark = state.theme === 'dark' || (state.theme === 'auto' && matchMedia('(prefers-color-scheme: dark)').matches);
    document.documentElement.dataset.theme = dark ? 'dark' : 'light';
    $('meta[name="theme-color"]').setAttribute('content', dark ? '#1A120E' : '#C8471A');
  }
  const applyFontSize = () => document.documentElement.style.setProperty('--verse', `${state.fontSize}px`);
  const applyLang = () => { document.documentElement.lang = en() ? 'en' : 'mr'; };

  function setState(key, value) {
    state[key] = value;
    store.set(key, value);
  }

  function toggleFavorite(id) {
    const f = state.favorites;
    setState('favorites', f.includes(id) ? f.filter(x => x !== id) : [id, ...f]);
  }

  function markRead(id) {
    const next = [id, ...state.recents.filter(x => x !== id)].slice(0, 8);
    if (next.join() !== state.recents.join()) setState('recents', next);
  }

  // ---------- Wake lock (keep the screen on while reading) ----------
  let wakeLock = null;
  async function setAwake(on) {
    if (!('wakeLock' in navigator)) return;
    try {
      if (on && !wakeLock) {
        wakeLock = await navigator.wakeLock.request('screen');
        wakeLock.addEventListener('release', () => { wakeLock = null; });
      } else if (!on && wakeLock) {
        await wakeLock.release();
        wakeLock = null;
      }
    } catch { wakeLock = null; }
  }

  // ---------- Toast ----------
  let toastTimer;
  function toast(msg, actionLabel, action) {
    const t = $('#toast');
    t.innerHTML = `<span>${esc(msg)}</span>${actionLabel ? `<button type="button">${esc(actionLabel)}</button>` : ''}`;
    t.hidden = false;
    if (actionLabel) t.querySelector('button').onclick = () => { t.hidden = true; action(); };
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.hidden = true; }, actionLabel ? 9000 : 3500);
  }

  // ---------- Shared pieces ----------
  function favButton(id) {
    const on = state.favorites.includes(id);
    return `<button type="button" class="icon-btn fav ${on ? 'on' : ''}" data-action="fav" data-id="${esc(id)}" aria-pressed="${on}" aria-label="${esc(on ? S().removeFav : S().addFav)}">${icon(on ? 'heart' : 'heart-o')}</button>`;
  }

  function itemCard(item, ids, index, title) {
    const d = lib.deityMap.get(item.deity);
    const c = lib.catMap.get(item.category);
    const meta = [c && catName(c), item.author || (d && deityName(d))].filter(Boolean).join(' • ');
    const second = en() ? item.titleEn || item._preview : item._preview;
    return `<div class="item" style="${deityStyle(d)}">
      <a class="item-main" href="${readHref(ids, index, title)}">${avatar(d, 48)}
        <span class="item-text"><b lang="mr">${esc(item.title)}</b><span>${esc(meta)}</span><small lang="${en() && item.titleEn ? 'en' : 'mr'}">${esc(second)}</small></span>
      </a>${favButton(item.id)}</div>`;
  }

  const itemList = (items, title) => {
    const ids = items.map(i => i.id);
    return items.map((it, k) => itemCard(it, ids, k, title)).join('');
  };

  const empty = (iconName, title, msg) =>
    `<div class="empty"><span class="bubble">${icon(iconName)}</span><h2>${esc(title)}</h2><p>${esc(msg)}</p></div>`;

  // ---------- Install prompt ----------
  let deferredPrompt = null;
  window.addEventListener('beforeinstallprompt', e => {
    e.preventDefault();
    deferredPrompt = e;
    if (route().name === 'home') render(true);
  });

  function installBanner() {
    if (isStandalone() || store.get('installDismissed', false)) return '';
    const close = `<button type="button" class="icon-btn" data-action="dismiss-install" aria-label="${esc(S().dismiss)}">${icon('close')}</button>`;
    if (isIOS()) {
      return `<div class="banner">${icon('download')}<div class="grow"><b>${esc(S().installTitle)}</b>
        <small>${S().installIOS(icon('ios-share', 'inline-ic'), icon('add-box', 'inline-ic'))}</small></div>${close}</div>`;
    }
    if (deferredPrompt) {
      return `<div class="banner">${icon('download')}<div class="grow"><b>${esc(S().installTitle)}</b>
        <small>${esc(S().installAndroid)}</small></div>
        <button type="button" class="btn-tonal" data-action="install">${esc(S().install)}</button>${close}</div>`;
    }
    return '';
  }

  // ---------- Screens ----------
  function homeView() {
    const s = S();
    const today = new Date();
    const d = lib.deityMap.get(DAY_DEITY[today.getDay()]);
    const todayItems = d ? lib.items.filter(i => i.deity === d.id) : [];
    const todayIds = todayItems.map(i => i.id);
    const recents = state.recents.map(id => lib.byId.get(id)).filter(Boolean);
    const deities = lib.deities.filter(x => lib.items.some(i => i.deity === x.id));

    return `
    <section class="hero">${RINGS}
      <div class="hero-in">
        <div class="hero-top"><span class="om" lang="mr">॥ श्री गणेशाय नमः ॥</span>
          <button type="button" class="lang-chip" data-action="toggle-lang">${icon('translate')}${esc(s.otherLang)}</button></div>
        <div class="hero-row"><div><h1>${esc(s.appName)}</h1><p class="tagline">${esc(s.tagline)}</p></div>
          <img class="diya" src="icons/diya.svg" alt="" width="96" height="96"></div>
        <span class="date-pill">${icon('sun')}${esc(dateText(today))}</span>
        <a class="search-pill" href="#/search?focus=1">${icon('search')}<span>${esc(s.searchHint)}</span></a>
      </div>
    </section>
    ${installBanner()}
    ${d && todayItems.length ? `
    <section class="panel" style="${deityStyle(d)}">
      <div class="today-head">${avatar(d, 52)}
        <div class="grow"><small>${esc(s.today(today))}</small><b>${esc(s.deityOfDay(deityName(d)))}</b></div>
        <a class="text-btn" href="#/list/deity/${esc(d.id)}">${esc(s.all)}</a></div>
      <div class="hscroll today-items">${todayItems.map((it, k) => `
        <a class="mini" style="${deityStyle(d)}" href="${readHref(todayIds, k, deityName(d))}">${catIcon(it.category)}
          <b lang="mr">${esc(it.title)}</b><small>${esc(catName(lib.catMap.get(it.category) || {}))}</small></a>`).join('')}
      </div>
    </section>` : ''}
    ${lib.playlists.length ? `<h2 class="section">${esc(s.sequences)}</h2>
    <div class="hscroll">${lib.playlists.map((p, k) => `
      <a class="playlist pl-${k % 4}" href="${readHref(p.items, 0, plName(p))}">
        <span class="pl-icon">${icon('queue')}</span><h3>${esc(plName(p))}</h3><p>${esc(plDesc(p))}</p>
        <span class="pl-foot"><span class="pl-start">${icon('play')}${esc(s.start)}</span><span class="pl-count">${esc(s.parts(p.items.length))}</span></span>
      </a>`).join('')}</div>` : ''}
    <h2 class="section">${esc(s.categories)}</h2>
    <div class="cat-grid">${lib.categories.map((c, k) => `
      <a class="cat cat-${k % 4}" href="#/list/category/${esc(c.id)}"><span class="cat-icon">${catIcon(c.id)}</span>
        <b>${esc(catName(c))}</b><small>${esc(s.texts(lib.items.filter(i => i.category === c.id).length))}</small></a>`).join('')}
    </div>
    <h2 class="section">${esc(s.deities)}</h2>
    <div class="hscroll deities">${deities.map(x => `
      <a class="deity" href="#/list/deity/${esc(x.id)}">${avatar(x, 62)}<span>${esc(deityShort(x))}</span></a>`).join('')}
    </div>
    ${recents.length ? `<h2 class="section">${esc(s.recent)}</h2>${itemList(recents, s.recent)}` : ''}
    <footer class="foot">${ORNAMENT}<div class="blessing" lang="mr">॥ शुभं भवतु ॥</div><small>${esc(s.footer)}</small></footer>`;
  }

  function listView(r) {
    const [, type, id] = r.parts;
    const d = type === 'deity' ? lib.deityMap.get(id) : null;
    const c = type === 'category' ? lib.catMap.get(id) : null;
    if (!d && !c) return homeRedirect();
    const title = d ? deityName(d) : catName(c);
    const items = lib.items.filter(i => (d ? i.deity === d.id : i.category === c.id));
    const sub = (c && catDesc(c)) || S().texts(items.length);
    return `
    <header class="appbar"><button type="button" class="icon-btn" data-action="back" aria-label="${esc(S().back)}">${icon('back')}</button>
      <div class="titles"><b>${esc(title)}</b></div></header>
    <section class="list-hero ${d ? 'deity-bg' : ''}" ${d ? `style="${deityStyle(d)}"` : ''}>
      ${d ? avatar(d, 64) : `<span class="cat-icon">${catIcon(c.id)}</span>`}
      <div><h2>${esc(title)}</h2><p>${esc(sub)}</p></div></section>
    ${itemList(items, title)}<div style="height:24px"></div>`;
  }

  function searchResults() {
    const results = search(searchState.q, searchState.cat);
    return `<div class="count">${esc(S().texts(results.length))}</div>${results.length
      ? itemList(results, S().results)
      : empty('search', S().noResults, S().noResultsHint)}`;
  }

  function searchChips() {
    const chip = (value, label) =>
      `<button type="button" class="chip ${searchState.cat === value ? 'on' : ''}" data-action="cat" data-value="${esc(value || '')}">${esc(label)}</button>`;
    return chip(null, S().all) + lib.categories.map(c => chip(c.id, catName(c))).join('');
  }

  function searchView() {
    const s = S();
    return `
    <div class="screen-head"><h1>${esc(s.tabSearch)}</h1><p>${esc(s.searchSub)}</p></div>
    <label class="search-box">${icon('search')}
      <input id="q" type="search" enterkeyhint="search" autocomplete="off" autocapitalize="off" spellcheck="false"
        placeholder="${esc(s.searchPh)}" value="${esc(searchState.q)}" aria-label="${esc(s.tabSearch)}">
      <button type="button" class="icon-btn" data-action="clear-search" aria-label="${esc(s.clear)}" ${searchState.q ? '' : 'hidden'}>${icon('close')}</button>
    </label>
    <div class="chips" id="chips">${searchChips()}</div>
    <div id="results">${searchResults()}</div>`;
  }

  function favView() {
    const s = S();
    const items = state.favorites.map(id => lib.byId.get(id)).filter(Boolean);
    return `<div class="screen-head"><h1>${esc(s.tabFav)}</h1><p>${esc(items.length ? s.favCount(items.length) : s.favEmptySub)}</p></div>
      ${items.length ? itemList(items, s.tabFav) : empty('heart-o', s.favEmptyTitle, s.favEmptyMsg)}`;
  }

  function settingsView() {
    const s = S();
    const seg = (action, current, options) => `<div class="segmented" role="group">${options.map(([value, label, ic]) => `
      <button type="button" class="${current === value ? 'on' : ''}" data-action="${action}" data-value="${value}" aria-pressed="${current === value}">
        ${current === value ? icon('check', 'sm') : ic ? icon(ic, 'sm') : ''}${esc(label)}</button>`).join('')}</div>`;
    const canWake = 'wakeLock' in navigator;
    return `
    <div class="screen-head"><h1>${esc(s.tabSettings)}</h1><p>${esc(s.settingsSub)}</p></div>
    <div class="group-title">${esc(s.langGroup)}</div>
    <div class="group"><h3>${esc(s.appLang)}</h3>
      ${seg('lang', state.lang, [['mr', 'मराठी'], ['en', 'English']])}
      <p class="note" style="margin-top:8px">${esc(s.langNote)}</p></div>

    <div class="group-title">${esc(s.listenGroup)}</div>
    <div class="group"><p class="note">${esc(s.listenNote)}</p>
      <h3 style="margin-top:14px">${esc(s.speechSpeed)}</h3>
      ${seg('speech', state.speechSlow ? 'slow' : 'normal', [['slow', s.slow], ['normal', s.normalSpeed]])}
      <div class="divider"></div>
      <div class="row"><h3 class="grow">${esc(s.musicSetting)}</h3>
        <button type="button" class="switch ${state.music ? 'on' : ''}" role="switch" aria-checked="${state.music}"
          aria-label="${esc(s.musicSetting)}" data-action="music-setting"></button></div>
      <p class="note" style="margin-top:10px">${esc('speechSynthesis' in window ? s.voiceStatus(marathiVoiceStatus()) : s.noSpeech)}</p>
    </div>

    <div class="group-title">${esc(s.appearance)}</div>
    <div class="group"><h3>${esc(s.theme)}</h3>
      ${seg('theme', state.theme, [['auto', s.auto, 'auto'], ['light', s.day, 'sun'], ['dark', s.night, 'moon']])}
      <div class="row" style="margin-top:18px"><h3 class="grow">${esc(s.textSize)}</h3><b id="size-value" style="color:var(--primary)">${esc(num(state.fontSize))}</b></div>
      <input type="range" min="16" max="40" step="1" value="${state.fontSize}" data-input="font" aria-label="${esc(s.textSize)}">
      <div class="verse preview-verse" lang="mr">॥ श्री गणेशाय नमः ॥</div>
      <div class="divider"></div>
      <div class="row"><div class="grow"><h3>${esc(s.keepAwake)}</h3><p class="note">${esc(canWake ? s.keepAwakeHint : s.keepAwakeNo)}</p></div>
        <button type="button" class="switch ${state.keepAwake && canWake ? 'on' : ''}" role="switch" aria-checked="${state.keepAwake && canWake}"
          aria-label="${esc(s.keepAwake)}" data-action="awake" ${canWake ? '' : 'disabled'}></button></div>
    </div>

    <div class="group-title">${esc(s.updates)}</div>
    <div class="group">
      <div class="row">${icon('book')}<div class="grow"><h3>${esc(s.collection)}</h3><p class="note">${esc(s.collectionInfo(lib.version, lib.items.length))}</p></div>
        <button type="button" class="btn-outline" data-action="check-content">${esc(s.checkNew)}</button></div>
      <p class="note" style="margin:4px 0 0 40px">${esc(s.otaNote)}</p>
    </div>

    <div class="group-title">${esc(s.about)}</div>
    <div class="group"><p>${esc(s.aboutText)}</p>
      <a class="link-row" href="${REPO}/issues/new" target="_blank" rel="noopener">${icon('feedback')}<span>${esc(s.report)}</span>${icon('open')}</a>
      <a class="link-row" href="${REPO}/releases/latest" target="_blank" rel="noopener">${icon('download')}<span>${esc(s.android)}</span>${icon('open')}</a>
      <a class="link-row" href="${REPO}" target="_blank" rel="noopener">${icon('code')}<span>${esc(s.source)}</span>${icon('open')}</a>
    </div>
    <footer class="foot">${ORNAMENT}<div class="blessing" lang="mr">॥ सर्वे भवन्तु सुखिनः ॥</div></footer>`;
  }

  function readerView(r) {
    const ids = (r.q.get('ids') || '').split(',').filter(id => lib.byId.has(id));
    if (!ids.length) return homeRedirect();
    const i = clamp(parseInt(r.q.get('i'), 10) || 0, 0, ids.length - 1);
    const item = lib.byId.get(ids[i]);
    const title = r.q.get('t') || '';
    const d = lib.deityMap.get(item.deity);
    const c = lib.catMap.get(item.category);
    const s = S();
    const about = aboutOf(item);
    const refrain = block => (item.refrain ? `<div class="refrain" lang="mr" data-b="${block}">${linesHtml(item.refrain)}</div>` : '');
    const meta = [d && deityName(d), c && catName(c), langName(item.language)].filter(Boolean).join(' • ');
    const pager = ids.length > 1;
    markRead(item.id);
    return `
    <header class="appbar"><button type="button" class="icon-btn" data-action="back" aria-label="${esc(s.back)}">${icon('back')}</button>
      <div class="titles"><b lang="mr">${esc(item.title)}</b>${ids.length > 1 ? `<small>${esc(title)}</small>` : ''}</div>
      <button type="button" class="icon-btn" data-action="size" aria-label="${esc(s.textSize)}">${icon('size')}</button>
      ${favButton(item.id)}
      <button type="button" class="icon-btn" data-action="share" data-id="${esc(item.id)}" aria-label="${esc(s.share)}">${icon('share')}</button>
    </header>
    <article class="reader ${ids.length > 1 ? 'with-pager' : ''}" style="${deityStyle(d)}" data-ids="${esc(ids.join(','))}" data-i="${i}" data-t="${esc(title)}">
      <div class="reader-head">${avatar(d, 60)}
        <h1 lang="mr">${esc(item.title)}</h1>
        ${en() && item.titleEn ? `<div class="tr">${esc(item.titleEn)}</div>` : ''}
        <div class="meta">${esc(meta)}</div>
        ${item.author ? `<div class="by">${esc(s.by(item.author))}</div>` : ''}
        ${ORNAMENT}</div>
      ${about ? `<div class="about">${icon('info')}<span>${esc(about)}</span></div>` : ''}
      ${item.refrainFirst ? refrain('rt') : ''}
      ${item.verses.map((v, k) => `<div class="verse" lang="mr" data-b="v${k}">${linesHtml(v)}</div>${refrain(`r${k}`)}`).join('')}
      ${ORNAMENT}
    </article>
    <button type="button" class="listen-fab ${pager ? 'above-pager' : ''}" data-action="listen">${icon('headphones')}${esc(s.listen)}</button>
    <div class="player ${pager ? 'above-pager' : ''}" id="player" hidden>
      <div class="player-progress"><i></i></div>
      <div class="player-row">
        <button type="button" class="play-btn" data-action="play-pause" aria-label="${esc(s.pause)}">${icon('pause')}</button>
        <div class="grow"><b id="player-line"></b><small id="player-note" hidden>${esc(s.hindiVoiceNote)}</small></div>
        <button type="button" class="chip ${state.speechSlow ? 'on' : ''}" data-action="speed" aria-pressed="${state.speechSlow}">${esc(s.slow)}</button>
        <button type="button" class="icon-btn music ${state.music ? 'on' : ''}" data-action="music" aria-pressed="${state.music}" aria-label="${esc(s.music)}">${icon(state.music ? 'note' : 'music-off')}</button>
        <button type="button" class="icon-btn" data-action="stop-listen" aria-label="${esc(s.stopListening)}">${icon('close')}</button>
      </div>
    </div>
    ${pager ? `
    <nav class="pager"><div class="pager-in">
      <button type="button" data-action="page" data-to="${i - 1}" ${i === 0 ? 'disabled' : ''}>${icon('chev-left')}${esc(s.prev)}</button>
      <div class="mid">${esc(num(i + 1))} / ${esc(num(ids.length))}<div class="progress"><i style="width:${((i + 1) / ids.length) * 100}%"></i></div></div>
      <button type="button" data-action="page" data-to="${i + 1}" ${i === ids.length - 1 ? 'disabled' : ''}>${esc(s.next)}${icon('chev-right')}</button>
    </div></nav>` : ''}`;
  }

  function readerItem() {
    const el = $('.reader');
    return el && lib.byId.get(el.dataset.ids.split(',')[Number(el.dataset.i)]);
  }

  function homeRedirect() {
    setTimeout(() => location.replace('#/'), 0);
    return '';
  }

  function goPage(to) {
    const el = $('.reader');
    if (!el) return;
    const ids = el.dataset.ids.split(',');
    if (to < 0 || to >= ids.length) return;
    location.replace(readHref(ids, to, el.dataset.t));
  }

  function openSizeSheet() {
    const s = S();
    const wrap = document.createElement('div');
    wrap.className = 'sheet-backdrop';
    wrap.innerHTML = `<div class="sheet" role="dialog" aria-modal="true" aria-label="${esc(s.textSize)}"><div class="handle"></div>
      <h2>${esc(s.textSize)}</h2>
      <div class="verse preview-verse" lang="mr" style="padding:16px 0">सुखकर्ता दुःखहर्ता</div>
      <div class="size-row"><button type="button" class="size-btn" data-size="-2" style="font-size:14px" lang="mr" aria-label="−">अ</button>
        <input type="range" min="16" max="40" step="1" value="${state.fontSize}" data-input="font" aria-label="${esc(s.textSize)}">
        <button type="button" class="size-btn" data-size="2" style="font-size:22px" lang="mr" aria-label="+">अ</button></div></div>`;
    wrap.addEventListener('click', e => {
      if (e.target === wrap) wrap.remove();
      const b = e.target.closest('[data-size]');
      if (b) {
        setFontSize(state.fontSize + Number(b.dataset.size));
        wrap.querySelector('input').value = state.fontSize;
      }
    });
    document.body.appendChild(wrap);
  }

  function setFontSize(v) {
    setState('fontSize', clamp(Math.round(v), 16, 40));
    applyFontSize();
    const label = $('#size-value');
    if (label) label.textContent = num(state.fontSize);
  }

  async function share(item) {
    const s = S();
    const parts = [`॥ ${item.title} ॥`];
    if (item.author) parts.push(s.by(item.author));
    parts.push('');
    if (item.refrainFirst && item.refrain) parts.push(item.refrain, '');
    for (const v of item.verses) {
      parts.push(v, '');
      if (item.refrain) parts.push(item.refrain, '');
    }
    parts.push(s.shareFooter);
    const text = parts.join('\n');
    if (navigator.share) {
      try { await navigator.share({ title: item.title, text }); } catch { /* cancelled */ }
    } else {
      try { await navigator.clipboard.writeText(text); toast(s.copied); } catch { /* no clipboard */ }
    }
  }

  async function checkContent() {
    try {
      const before = lib.items.length;
      const fresh = await fetchContent(true);
      if (fresh.version > lib.version) {
        lib = fresh;
        render(true);
        toast(S().contentUpdated(fresh.items.length - before));
      } else {
        toast(S().upToDate);
      }
    } catch {
      toast(S().offline);
    }
  }

  // ---------- Read aloud (speech + tanpura) ----------
  /** Each line in its own span so the line being read can be highlighted. */
  function linesHtml(text) {
    return text.split('\n').map((line, k) => `<span class="ln" data-l="${k}">${esc(line)}</span>`).join('\n');
  }

  const cleanLine = line => line
    .replace(/॥\s*[०-९0-9]+\s*॥/g, ' ')
    .replace(/[।॥'"“”‘’()०-९0-9]/g, ' ')
    .replace(/—/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

  /** The lines exactly as shown on screen (refrain after every verse), ready to speak. */
  function recitationScript(item) {
    const out = [];
    const add = (block, text) => text.split('\n').forEach((raw, line) => {
      const text2 = cleanLine(raw);
      if (text2) out.push({ block, line, text: text2 });
    });
    if (item.refrain && item.refrainFirst) add('rt', item.refrain);
    item.verses.forEach((v, k) => {
      add(`v${k}`, v);
      if (item.refrain) add(`r${k}`, item.refrain);
    });
    return out;
  }

  const speechLang = item => (item.language === 'हिंदी' || item.language === 'अवधी' ? 'hi' : 'mr');

  function findVoice(prefix) {
    const voices = 'speechSynthesis' in window ? speechSynthesis.getVoices() : [];
    const matches = voices.filter(v => v.lang.replace('_', '-').toLowerCase().startsWith(prefix));
    // Prefer natural/online voices when the browser offers several.
    return matches.find(v => /natural|online|google/i.test(v.name)) || matches[0] || null;
  }

  /** 'mr', 'hi' or '' — which voice the browser can use for Marathi text. */
  function marathiVoiceStatus() {
    if (findVoice('mr')) return 'mr';
    if (findVoice('hi')) return 'hi';
    return '';
  }

  const speech = { status: 'idle', itemId: null, index: -1, script: [], gen: 0, hindi: false, autoNext: false };

  function listen(item, from = 0) {
    if (!('speechSynthesis' in window)) { toast(S().noSpeech); return; }
    const script = recitationScript(item);
    if (!script.length) return;
    const gen = ++speech.gen;
    speechSynthesis.cancel();
    const wanted = speechLang(item);
    // Marathi and Hindi voices can each read the other's Devanagari, so fall back either way.
    const voice = findVoice(wanted) || findVoice(wanted === 'hi' ? 'mr' : 'hi');
    if (!voice && speechSynthesis.getVoices().length) {
      stopSpeech();
      toast(S().noVoice);
      return;
    }
    Object.assign(speech, {
      status: 'playing', itemId: item.id, index: from, script,
      hindi: wanted === 'mr' && !!voice && voice.lang.toLowerCase().startsWith('hi'),
    });
    for (let k = from; k < script.length; k++) {
      const u = new SpeechSynthesisUtterance(script[k].text);
      u.lang = voice ? voice.lang : (wanted === 'hi' ? 'hi-IN' : 'mr-IN');
      if (voice) u.voice = voice;
      u.rate = state.speechSlow ? 0.65 : 0.9;
      u.onstart = () => {
        if (gen !== speech.gen) return;
        speech.index = k;
        updatePlayer();
      };
      if (k === script.length - 1) {
        u.onend = () => {
          if (gen !== speech.gen) return;
          stopSpeech();
          nextInSequence();
        };
      }
      u.onerror = e => {
        if (gen !== speech.gen || e.error === 'interrupted' || e.error === 'canceled') return;
        stopSpeech();
      };
      speechSynthesis.speak(u);
    }
    startMusic();
    updatePlayer();
  }

  function pauseSpeech() {
    speech.gen++;
    speechSynthesis.cancel();
    speech.status = 'paused';
    stopMusic();
    updatePlayer();
  }

  function stopSpeech() {
    speech.gen++;
    if ('speechSynthesis' in window) speechSynthesis.cancel();
    Object.assign(speech, { status: 'idle', itemId: null, index: -1, script: [] });
    stopMusic();
    updatePlayer();
  }

  function nextInSequence() {
    const el = $('.reader');
    if (!el) return;
    const ids = el.dataset.ids.split(',');
    const next = Number(el.dataset.i) + 1;
    if (next >= ids.length) return;
    speech.autoNext = true;
    goPage(next);
  }

  /** Updates the player bar and the highlighted line without re-rendering the page. */
  function updatePlayer() {
    const player = $('#player');
    const fab = $('.listen-fab');
    const reader = $('.reader');
    if (!player || !reader) return;
    const ids = reader.dataset.ids.split(',');
    const here = ids[Number(reader.dataset.i)];
    const active = speech.status !== 'idle' && speech.itemId === here;
    player.hidden = !active;
    fab.hidden = active;
    reader.classList.toggle('with-player', active);
    reader.querySelectorAll('.ln.on').forEach(n => n.classList.remove('on'));
    if (!active) return;
    const s = S();
    const playing = speech.status === 'playing';
    const btn = player.querySelector('[data-action="play-pause"]');
    btn.innerHTML = icon(playing ? 'pause' : 'play');
    btn.setAttribute('aria-label', playing ? s.pause : s.resume);
    $('#player-line').textContent = s.lineOf(speech.index + 1, speech.script.length);
    $('#player-note').hidden = !speech.hindi;
    player.querySelector('.player-progress i').style.width = `${((speech.index + 1) / speech.script.length) * 100}%`;
    const line = speech.script[speech.index];
    const node = line && reader.querySelector(`[data-b="${line.block}"] [data-l="${line.line}"]`);
    if (node) {
      node.classList.add('on');
      const r = node.getBoundingClientRect();
      if (r.top < 90 || r.bottom > window.innerHeight - 170) node.scrollIntoView({ block: 'center', behavior: 'smooth' });
    }
  }

  // Soft tanpura drone (audio/tanpura.wav, made by scripts/make_tanpura.py), looped quietly.
  let audioCtx = null;
  let droneBuffer = null;
  let droneSource = null;

  /** Must run inside a tap on iPhone, or the browser keeps the audio muted. */
  function unlockAudio() {
    try {
      audioCtx = audioCtx || new (window.AudioContext || window.webkitAudioContext)();
      if (audioCtx.state === 'suspended') audioCtx.resume();
    } catch { audioCtx = null; }
  }

  async function startMusic() {
    if (!state.music || !audioCtx || droneSource) return;
    try {
      if (!droneBuffer) {
        const data = await (await fetch('audio/tanpura.wav')).arrayBuffer();
        droneBuffer = await new Promise((ok, fail) => audioCtx.decodeAudioData(data, ok, fail));
      }
      if (droneSource || speech.status !== 'playing') return;
      const gain = audioCtx.createGain();
      gain.gain.value = 0.18;
      gain.connect(audioCtx.destination);
      droneSource = audioCtx.createBufferSource();
      droneSource.buffer = droneBuffer;
      droneSource.loop = true;
      droneSource.connect(gain);
      droneSource.start();
    } catch { droneSource = null; }
  }

  function stopMusic() {
    if (!droneSource) return;
    try { droneSource.stop(); } catch { /* already stopped */ }
    droneSource.disconnect();
    droneSource = null;
  }

  // ---------- Router ----------
  const TABS = [
    { id: 'home', href: '#/', icon: 'home', label: 'tabHome' },
    { id: 'search', href: '#/search', icon: 'search', label: 'tabSearch' },
    { id: 'fav', href: '#/fav', icon: 'heart', label: 'tabFav' },
    { id: 'settings', href: '#/settings', icon: 'settings', label: 'tabSettings' },
  ];
  const scrolls = {};
  let lastKey = null;
  let inAppNavigations = 0;

  function route() {
    const raw = location.hash.replace(/^#/, '') || '/';
    const [path, qs] = raw.split('?');
    const parts = path.split('/').filter(Boolean);
    return { name: parts[0] || 'home', parts, q: new URLSearchParams(qs || ''), key: raw };
  }

  function renderTabs(active) {
    const nav = $('#tabs');
    nav.hidden = !active;
    if (!active) return;
    nav.innerHTML = `<div class="tabs-in">${TABS.map(t => `
      <a class="tab ${t.id === active ? 'on' : ''}" href="${t.href}" ${t.id === active ? 'aria-current="page"' : ''}>
        <span class="pill">${icon(t.icon)}</span>${esc(S()[t.label])}</a>`).join('')}</div>`;
  }

  function render(keepScroll) {
    if (!lib) return;
    const r = route();
    const y = window.scrollY;
    if (lastKey && lastKey !== r.key) scrolls[lastKey] = y;
    const views = { home: homeView, search: searchView, fav: favView, settings: settingsView, list: listView, read: readerView };
    const view = views[r.name] || homeView;
    const tab = { home: 'home', search: 'search', fav: 'fav', settings: 'settings' }[r.name] || (views[r.name] ? null : 'home');
    const app = $('#app');
    app.innerHTML = view(r);
    app.classList.toggle('no-tabs', !tab);
    renderTabs(tab);
    window.scrollTo(0, keepScroll ? y : r.name === 'read' ? 0 : scrolls[r.key] || 0);
    lastKey = r.key;

    setAwake(r.name === 'read' && state.keepAwake);
    // Read aloud: carry on through a sequence, switch with a swipe, stop when leaving the reader.
    if (r.name === 'read') {
      const reader = $('.reader');
      const here = reader && reader.dataset.ids.split(',')[Number(reader.dataset.i)];
      if (speech.itemId && speech.itemId !== here) {
        if (speech.status === 'playing') listen(lib.byId.get(here)); else stopSpeech();
      } else if (speech.autoNext && speech.status === 'idle' && here) {
        listen(lib.byId.get(here));
      }
      speech.autoNext = false;
      updatePlayer();
    } else if (speech.status !== 'idle') {
      stopSpeech();
    }
    if (r.name === 'search') {
      const input = $('#q');
      if (r.q.get('focus')) input.focus();
    }
  }

  // ---------- Events ----------
  document.addEventListener('click', e => {
    const el = e.target.closest('[data-action]');
    if (!el) return;
    const a = el.dataset.action;
    if (a === 'fav') { toggleFavorite(el.dataset.id); render(true); }
    else if (a === 'back') { if (inAppNavigations > 1) history.back(); else location.replace('#/'); }
    else if (a === 'toggle-lang' || a === 'lang') {
      setState('lang', a === 'lang' ? el.dataset.value : en() ? 'mr' : 'en');
      applyLang();
      render(true);
    }
    else if (a === 'theme') { setState('theme', el.dataset.value); applyTheme(); render(true); }
    else if (a === 'awake') { setState('keepAwake', !state.keepAwake); render(true); }
    else if (a === 'listen') { unlockAudio(); listen(readerItem()); }
    else if (a === 'play-pause') {
      if (speech.status === 'playing') pauseSpeech();
      else { unlockAudio(); listen(readerItem(), Math.max(0, speech.index)); }
    }
    else if (a === 'speed') {
      setState('speechSlow', !state.speechSlow);
      el.classList.toggle('on', state.speechSlow);
      el.setAttribute('aria-pressed', state.speechSlow);
      if (speech.status === 'playing') listen(readerItem(), Math.max(0, speech.index));
    }
    else if (a === 'music') {
      setState('music', !state.music);
      el.classList.toggle('on', state.music);
      el.setAttribute('aria-pressed', state.music);
      el.innerHTML = icon(state.music ? 'note' : 'music-off');
      if (state.music && speech.status === 'playing') { unlockAudio(); startMusic(); } else stopMusic();
    }
    else if (a === 'stop-listen') stopSpeech();
    else if (a === 'speech') { setState('speechSlow', el.dataset.value === 'slow'); render(true); }
    else if (a === 'music-setting') { setState('music', !state.music); render(true); }
    else if (a === 'size') openSizeSheet();
    else if (a === 'share') share(lib.byId.get(el.dataset.id));
    else if (a === 'page') goPage(Number(el.dataset.to));
    else if (a === 'check-content') checkContent();
    else if (a === 'dismiss-install') { store.set('installDismissed', true); render(true); }
    else if (a === 'install' && deferredPrompt) {
      deferredPrompt.prompt();
      deferredPrompt.userChoice.finally(() => { deferredPrompt = null; render(true); });
    }
    else if (a === 'cat') {
      searchState.cat = el.dataset.value || null;
      $('#chips').innerHTML = searchChips();
      $('#results').innerHTML = searchResults();
    }
    else if (a === 'clear-search') {
      searchState.q = '';
      const input = $('#q');
      input.value = '';
      input.focus();
      el.hidden = true;
      $('#results').innerHTML = searchResults();
    }
  });

  document.addEventListener('input', e => {
    if (e.target.id === 'q') {
      searchState.q = e.target.value;
      $('[data-action="clear-search"]').hidden = !searchState.q;
      $('#results').innerHTML = searchResults();
    } else if (e.target.dataset.input === 'font') {
      setFontSize(Number(e.target.value));
    }
  });

  document.addEventListener('keydown', e => {
    if (e.target.id === 'q' && e.key === 'Enter') e.target.blur();
    if (route().name !== 'read' || e.target.closest('input')) return;
    const el = $('.reader');
    if (!el) return;
    if (e.key === 'ArrowRight') goPage(Number(el.dataset.i) + 1);
    if (e.key === 'ArrowLeft') goPage(Number(el.dataset.i) - 1);
  });

  // Swipe left/right in the reader to move through a sequence.
  let touchX = 0;
  let touchY = 0;
  document.addEventListener('touchstart', e => {
    touchX = e.touches[0].clientX;
    touchY = e.touches[0].clientY;
  }, { passive: true });
  document.addEventListener('touchend', e => {
    const el = e.target.closest('.reader');
    if (!el) return;
    const dx = e.changedTouches[0].clientX - touchX;
    const dy = e.changedTouches[0].clientY - touchY;
    if (Math.abs(dx) > 70 && Math.abs(dx) > 1.5 * Math.abs(dy)) goPage(Number(el.dataset.i) + (dx < 0 ? 1 : -1));
  }, { passive: true });

  window.addEventListener('hashchange', () => { inAppNavigations++; render(); });
  // Browsers load their voice list late; refresh the voice note in Settings when it arrives.
  if ('speechSynthesis' in window) {
    speechSynthesis.addEventListener?.('voiceschanged', () => { if (route().name === 'settings') render(true); });
  }
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible' && route().name === 'read' && state.keepAwake) setAwake(true);
  });
  matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => { if (state.theme === 'auto') applyTheme(); });

  // ---------- Offline support ----------
  if ('serviceWorker' in navigator) {
    const hadController = !!navigator.serviceWorker.controller;
    navigator.serviceWorker.register('sw.js').then(reg => {
      // Home-screen apps can stay open for days; look for a new version whenever it comes back.
      document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') reg.update().catch(() => {});
      });
    }).catch(() => {});
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      if (hadController) toast(S().appUpdated, S().refresh, () => location.reload());
    });
  }

  // ---------- Start ----------
  async function start() {
    applyTheme();
    applyLang();
    applyFontSize();
    try {
      lib = await fetchContent(false);
      inAppNavigations = 1;
      render();
    } catch {
      $('#app').innerHTML = `<div class="empty"><span class="bubble">${icon('info')}</span><h2>${esc(S().appName)}</h2>
        <p>${esc(S().loadError)}</p><p style="margin-top:16px"><button type="button" class="btn-filled" onclick="location.reload()">${esc(S().retry)}</button></p></div>`;
    }
  }
  start();
})();
