package com.aft.api.agent.routing;

import com.aft.api.agent.tool.ToolNames;
import java.text.Normalizer;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class IntentRouter {
    private static final int MARGIN = 2;
    private static final int SHORT_WORD = 3;


    private static final Set<String> GREETINGS = Set.of(
            "merhaba", "selam", "gunaydin", "iyi gunler", "iyi aksamlar", "nasilsin",
            "tesekkur", "tesekkurler", "sagol", "sag ol", "eyvallah", "gorusuruz",
            "hello", "hi", "hey", "thanks", "thank you");

    private static final Set<String> SMALL_TALK = Set.of(
            "kimsin", "sen kimsin", "ne yapabilirsin", "neler yapabilirsin", "yeteneklerin",
            "nedir", "ne demek", "ne ise yarar", "farki nedir", "aciklar misin", "anlat",
            "ornek ver", "nasil calisir", "who are you", "what can you do",
            "yapabilirsin", "yapabiliyorsun", "aciklar", "aciklayabilir");

    private static final Set<String> ACTIONS = Set.of(
            "calistir", "kos", "tara", "olustur", "yarat", "kaydet", "yaz", "sil", "kaldir",
            "ac", "listele", "goster", "getir", "bul", "ara", "dur", "durdur", "iptal",
            "tikla", "gir", "dogrula", "kontrol et", "raporla", "analiz et", "duzelt",
            "guncelle", "tasi", "yenile", "baslat", "devam et", "tekrar dene");

    private static final Set<String> DOMAIN = Set.of(
            "senaryo", "test", "adim", "kosum", "run", "sayfa", "tarayici", "element",
            "descriptor", "selector", "secici", "rapor", "klasor", "kutuphane", "taslak",
            "form", "buton", "alan", "url", "adres", "hata", "baglam", "saglik", "kirilgan");

    private static final Set<String> CONFIRMS = Set.of(
            "evet", "tamam", "olur", "devam", "onayliyorum", "yap", "hadi", "peki", "ok", "okey");

    private static final Map<ToolIntent, Set<String>> KEYWORDS = new EnumMap<>(ToolIntent.class);
    private static final Map<ToolIntent, Set<String>> TOOLS = new EnumMap<>(ToolIntent.class);

    private static final Set<String> DISCOVERY = Set.of(
            ToolNames.PAGE_STATE, ToolNames.LOCAL_SCENARIO_LIST, ToolNames.LOCAL_SCENARIO_SEARCH);

    static {
        KEYWORDS.put(ToolIntent.BROWSER, Set.of("sayfa", "tarayici", "element", "form", "buton",
                "tikla", "url", "adres", "tara", "snapshot", "ekran", "alan", "secici", "descriptor"));
        KEYWORDS.put(ToolIntent.SCENARIO, Set.of("senaryo", "adim", "taslak", "klasor", "kutuphane",
                "olustur", "kaydet", "sil", "dogrula", "test"));
        KEYWORDS.put(ToolIntent.RUN, Set.of("kosum", "run", "calistir", "kos", "sonuc", "gecti",
                "basarisiz", "iptal", "durdur", "rapor", "tekrar"));
        KEYWORDS.put(ToolIntent.DIAGNOSTIC, Set.of("saglik", "kirilgan", "istatistik", "ozet",
                "durum", "gecmis", "neden", "analiz", "hata", "baglam"));

        TOOLS.put(ToolIntent.BROWSER, Set.of(ToolNames.PAGE_STATE, ToolNames.PAGE_SNAPSHOT,
                ToolNames.BROWSER_COMMAND, ToolNames.LOCAL_DESCRIPTOR_SEARCH,
                ToolNames.SCENARIO_DRAFT_WRITE));
        TOOLS.put(ToolIntent.SCENARIO, Set.of(ToolNames.LOCAL_SCENARIO_LIST,
                ToolNames.LOCAL_SCENARIO_SEARCH, ToolNames.LOCAL_SCENARIO_READ,
                ToolNames.LOCAL_SCENARIO_VALIDATE, ToolNames.LOCAL_SCENARIO_DELETE,
                ToolNames.SCENARIO_DRAFT_WRITE, ToolNames.PAGE_SNAPSHOT));
        TOOLS.put(ToolIntent.RUN, Set.of(ToolNames.LOCAL_SCENARIO_RUN, ToolNames.LOCAL_RUN_CANCEL,
                ToolNames.LOCAL_RUN_HISTORY, ToolNames.LOCAL_RUN_DETAIL,
                ToolNames.LOCAL_FAILURE_CONTEXT, ToolNames.LOCAL_SCENARIO_SEARCH));
        TOOLS.put(ToolIntent.DIAGNOSTIC, Set.of(ToolNames.LOCAL_HEALTH_REPORT,
                ToolNames.LOCAL_RUN_HISTORY, ToolNames.LOCAL_RUN_DETAIL,
                ToolNames.LOCAL_FAILURE_CONTEXT, ToolNames.LOCAL_DESCRIPTOR_SEARCH));
    }

    public ToolIntent route(String content, boolean hasHistory) {
        String text = fold(content);
        if (text.isEmpty()) {
            return ToolIntent.CHAT;
        }
        if (hasHistory && isConfirmation(text)) {
            return ToolIntent.ANY;
        }

        boolean hasAction = containsAny(text, ACTIONS);
        if (!hasAction && isSmallTalk(text)) {
            return ToolIntent.CHAT;
        }
        if (!hasAction && !containsAny(text, DOMAIN)) {
            return ToolIntent.CHAT;
        }
        return best(text);
    }

    public Set<String> toolsFor(ToolIntent intent) {
        if (intent == ToolIntent.CHAT) {
            return Set.of();
        }
        if (intent == ToolIntent.ANY) {
            return Set.of();
        }
        Set<String> allowed = new LinkedHashSet<>(TOOLS.get(intent));
        allowed.addAll(DISCOVERY);
        return allowed;
    }

    public boolean offersTools(ToolIntent intent) {
        return intent != ToolIntent.CHAT;
    }

    private ToolIntent best(String text) {
        ToolIntent winner = ToolIntent.ANY;
        int top = 0;
        int second = 0;

        for (Map.Entry<ToolIntent, Set<String>> entry : KEYWORDS.entrySet()) {
            int score = score(text, entry.getValue());
            if (score > top) {
                second = top;
                top = score;
                winner = entry.getKey();
            } else if (score > second) {
                second = score;
            }
        }
        return top >= second + MARGIN ? winner : ToolIntent.ANY;
    }

    private int score(String text, Set<String> words) {
        int hits = 0;
        for (String word : words) {
            if (matches(text, word)) {
                hits++;
            }
        }
        return hits;
    }

    private boolean matches(String text, String word) {
        if (word.indexOf(' ') >= 0) {
            return text.contains(word);
        }
        for (String token : text.split(" ")) {
            if (word.length() <= SHORT_WORD ? token.equals(word) : token.startsWith(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConfirmation(String text) {
        if (text.length() > 24) {
            return false;
        }
        return CONFIRMS.stream().anyMatch(word -> text.equals(word) || text.startsWith(word + " "));
    }

    private boolean isSmallTalk(String text) {
        return containsAny(text, GREETINGS) || containsAny(text, SMALL_TALK);
    }

    private boolean containsAny(String text, Set<String> words) {
        return words.stream().anyMatch(word -> matches(text, word));
    }

    private String fold(String raw) {
        if (raw == null) {
            return "";
        }
        String lower = raw.toLowerCase(Locale.forLanguageTag("tr"));
        String plain = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('ı', 'i')
                .replace('ş', 's')
                .replace('ğ', 'g')
                .replace('ç', 'c')
                .replace('ö', 'o')
                .replace('ü', 'u');
        return plain.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    public List<ToolIntent> known() {
        return List.of(ToolIntent.values());
    }
}
