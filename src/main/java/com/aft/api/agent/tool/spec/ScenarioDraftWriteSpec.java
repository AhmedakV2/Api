package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class ScenarioDraftWriteSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.SCENARIO_DRAFT_WRITE;
    }

    @Override
    public String description() {
        return "Senaryo taslagini istemciye yazar. id bos birakilirsa yeni senaryo acilir. "
                + "Element uzerinde calisan adimlar (click, type, hover gibi) target olmadan kabul edilmez; "
                + "target degerini page_snapshot ciktisindaki element.target alanindan al. "
                + "Kullanici onayi gerektirir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": { "type": "string", "description": "Bos birakilirsa istemci yeni kimlik uretir" },
                    "title": { "type": "string" },
                    "description": { "type": "string", "default": "" },
                    "baseUrl": { "type": "string", "description": "Senaryonun basladigi adres" },
                    "folder": { "type": "string", "description": "Hedef klasor, bos ise kok" },
                    "steps": {
                      "type": "array",
                      "minItems": 1,
                      "items": {
                        "type": "object",
                        "properties": {
                          "kind": {
                            "type": "string",
                            "enum": ["click", "double-click", "right-click", "hover", "type",
                                     "clear-type", "press-key", "scroll", "select-option", "upload",
                                     "navigate", "wait", "refresh", "assert", "group"]
                          },
                          "title": { "type": "string", "description": "Adimi ozetleyen kisa baslik" },
                          "target": {
                            "type": "object",
                            "description": "Element adimlarinda zorunlu. Asagidaki alanlardan birini doldur; \
sirasiyla testId en saglami, text en kirilganidir.",
                            "properties": {
                              "testId": { "type": "string", "description": "data-testid ve benzeri test niteligi" },
                              "elementId": { "type": "string", "description": "id niteligi" },
                              "fieldName": { "type": "string", "description": "form alani name niteligi" },
                              "name": { "type": "string", "description": "erisilebilir ad" },
                              "text": { "type": "string", "description": "gorunen metin" },
                              "descriptorId": { "type": "string", "description": "katalogdaki descriptor kimligi" },
                              "ordinal": { "type": "integer", "minimum": 0, "description": "son care sira numarasi" }
                            }
                          },
                          "text": { "type": "string", "description": "type ve clear-type icin yazilacak deger" },
                          "key": { "type": "string", "description": "press-key icin tus adi" },
                          "url": { "type": "string", "description": "navigate icin adres" },
                          "deltaY": { "type": "integer", "description": "scroll icin dikey mesafe" },
                          "optionValue": { "type": "string", "description": "select-option icin deger" },
                          "waitMs": { "type": "integer", "minimum": 0, "description": "wait icin sure" },
                          "timeoutMs": { "type": "integer", "minimum": 0 },
                          "retries": { "type": "integer", "minimum": 0 },
                          "continueOnFailure": { "type": "boolean", "default": false },
                          "assertion": {
                            "type": "object",
                            "description": "assert adimlarinda zorunlu",
                            "properties": {
                              "kind": {
                                "type": "string",
                                "enum": ["element-exists", "element-absent", "element-visible",
                                         "element-enabled", "element-checked", "element-count",
                                         "text-equals", "text-contains", "value-equals",
                                         "attribute-equals", "url-matches", "title-contains"]
                              },
                              "target": { "type": "object", "description": "Adimdaki target ile ayni bicim" },
                              "expected": { "type": "string" },
                              "attribute": { "type": "string" },
                              "count": { "type": "integer", "minimum": 0 },
                              "soft": { "type": "boolean", "default": false }
                            },
                            "required": ["kind"]
                          },
                          "steps": {
                            "type": "array",
                            "items": { "type": "object" },
                            "description": "group adimlari icin alt adimlar"
                          }
                        },
                        "required": ["kind"]
                      }
                    }
                  },
                  "required": ["title", "baseUrl", "steps"]
                }
                """;
    }

    @Override
    public boolean writeEffect() {
        return true;
    }
}
