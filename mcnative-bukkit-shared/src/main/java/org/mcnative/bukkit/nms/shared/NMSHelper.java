package org.mcnative.bukkit.nms.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER,ElementType.LOCAL_VARIABLE,ElementType.CONSTRUCTOR,ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface NMSHelper {

}
