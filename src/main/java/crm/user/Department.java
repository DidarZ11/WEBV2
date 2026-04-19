package crm.user;

import lombok.Getter;

@Getter
public enum Department {
    RETAIL_LOANS("Кредитование и рассрочки"),
    CALL_CENTER("Колл-центр"),
    DIGITAL_IT("Мобильное приложение"),
    QUALITY_CONTROL("Контроль качества"),
    SECURITY("Безопасность");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }
}