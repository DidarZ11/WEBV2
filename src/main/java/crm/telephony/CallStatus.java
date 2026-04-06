package crm.telephony;

public enum CallStatus {
    NEW,          // Только позвонили, никто не взял трубку
    IN_PROGRESS,  // Оператор принял звонок
    COMPLETED,    // Разговор завершен
    MISSED        // Клиент сбросил до ответа
}