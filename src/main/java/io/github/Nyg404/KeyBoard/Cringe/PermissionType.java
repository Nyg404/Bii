package io.github.Nyg404.KeyBoard.Cringe;

public enum PermissionType {
    BAN("ban", "ban_level"),
    KICK("kick", "kick_level"),
    SLAP("slap", "slap_level");

    private final String permissionName;
    private final String columnName;

    PermissionType(String permissionName, String columnName) {
        this.permissionName = permissionName;
        this.columnName = columnName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getColumnName() {
        return columnName;
    }

    public static PermissionType fromString(String text) {
        for (PermissionType type : PermissionType.values()) {
            System.out.println("Сравниваем с: " + type.getPermissionName()); // Отладка
            if (type.permissionName.equalsIgnoreCase(text)) {
                System.out.println("Выбран: " + type.name()); // Отладка
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип прав: " + text);
    }
    
}