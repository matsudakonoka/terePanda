package com.cnpc.epai.core.worktask.util;

import lombok.Getter;
import lombok.Setter;

public class EnumUtil {

    public enum UnitTypes{
        TM001("TM001","采油气所"),

        TM002("TM002","地面所"),

        TM003("TM003","企业专家"),

        TM004("TM004","钻井所");

        @Getter
        @Setter
        private String display;

        @Getter
        @Setter
        private String value;

        public static String getDisplay(String value) {
            for (UnitTypes t : UnitTypes.values()) {
                if (t.getValue() == value) {
                    return t.display;
                }
            }
            return null;
        }


        private UnitTypes(String value, String name) {
            this.value = value;
            this.display = name;
        }

        public String value() {
            return this.value;
        }
    }
}
