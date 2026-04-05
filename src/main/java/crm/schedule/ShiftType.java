package crm.schedule;

public enum ShiftType {
    WORK,        // обычный рабочий день
    MORNING,     // утренняя смена
    EVENING,     // вечерняя смена
    NIGHT,       // ночная смена
    DAY_OFF,     // выходной
    SICK,        // больничный
    VACATION,    // отпуск
    BUSINESS_TRIP, // командировка
    UNPAID_LEAVE   // отпуск без содержания
}