package org.mcnative.common.serviceprovider.message.builder;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.entry.DocumentEntry;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.message.bml.builder.BuildContext;
import org.mcnative.common.connection.MinecraftConnection;
import org.mcnative.common.serviceprovider.message.ColoredString;
import org.mcnative.common.serviceprovider.message.builder.context.MinecraftBuildContext;
import org.mcnative.common.text.Text;
import org.mcnative.common.text.components.MessageComponent;
import org.mcnative.common.text.components.MessageKeyComponent;
import org.mcnative.common.text.format.TextColor;
import org.mcnative.common.text.format.TextStyle;

import java.lang.reflect.Array;
import java.util.Arrays;

public class TextBuildUtil {

    public static String buildUnformattedText(Object input,Object next) {
        String content = input.toString();
        if(next == null) return content;
        else return content+next;
    }

    public static String buildPlainText(Object input,Object nextComp){
        if(input instanceof ColoredString){
            String content = input.toString();

            StringBuilder builder = new StringBuilder(content);
            for (int i = 0; i < builder.length(); i++) {
                char char0 = builder.charAt(i);
                if((char0 == Text.FORMAT_CHAR || char0 == Text.DEFAULT_ALTERNATE_COLOR_CHAR) && builder.length() > ++i){
                    builder.delete(i-1,i+1);
                    i -= 2;
                }
            }

            if(nextComp != null)builder.append(buildPlainText(nextComp,null));

            return builder.toString();
        }else{
            String content = input.toString();
            if(nextComp != null) content += buildPlainText(nextComp,null);
            return content;
        }
    }

    private static void resetDocument(Document document){
        for (TextStyle value : TextStyle.values()) {
            if(value != TextStyle.RESET){
                document.set(value.getName(),false);
            }
        }
    }

    protected static Document buildCompileText(MinecraftBuildContext context,Object input,Object nextComp){
        if(input instanceof MessageKeyComponent){
            Document result = ((MessageKeyComponent) input).compile(context);
            return buildCompileText(context, result, nextComp);
        }else if(input instanceof MessageComponent){
            Document result = ((MessageComponent<?>) input).compile((MinecraftConnection)null,context.getVariables(),context.getLanguage());
            return buildCompileText(context, result, nextComp);
        }else if(input instanceof Document){
            return buildCompileText(context, (Document)input, nextComp);
        }else {
            Document root = Document.newDocument();
            Document current = root;
            if(input instanceof ColoredString){
                String content = input.toString();
                char[] chars = content.toCharArray();
                current.set("text","");

                int textIndex = 0;
                int lastColor = 0;
                for (int i = 0; i < chars.length; i++) {
                    char char0 = chars[i];
                    if((char0 == Text.FORMAT_CHAR || char0 == Text.DEFAULT_ALTERNATE_COLOR_CHAR) && chars.length > ++i){
                        TextColor color = TextColor.of(chars[i]);
                        if(color != null){
                            Document next = Document.newDocument();
                            current.set("extra",new Document[]{next});
                            next.set("color",color.getName());
                            next.set("text","");
                            resetDocument(next);
                            if(textIndex < i){
                                current.set("text",new String(Arrays.copyOfRange(chars,textIndex,i-1)));
                            }
                            current = next;
                            textIndex = i+1;
                            lastColor = i;
                        }

                        TextStyle style = TextStyle.of(chars[i]);
                        if(style != null){
                            if(i-lastColor != 2){
                                Document next = Document.newDocument();
                                current.set("extra",new Document[]{next});
                                resetDocument(next);
                                if(textIndex < i){
                                    current.set("text",new String(Arrays.copyOfRange(chars,textIndex,i-1)));
                                }
                                current = next;
                            }
                            current.set(style.getName().toLowerCase(),true);
                            textIndex = i+1;
                        }
                    }
                }
                if(textIndex < chars.length){
                    current.set("text",new String(Arrays.copyOfRange(chars,textIndex,chars.length)));
                }
            }else{
                String content = input.toString();
                root.set("text",content);
            }
            if(nextComp != null){
                if(nextComp instanceof DocumentEntry){
                    current.set("extra",new Object[]{nextComp});
                }else if(nextComp.getClass().isArray()){
                    int length = Array.getLength(nextComp);
                    if(length >= 0){
                        current.set("extra",nextComp);
                    }
                }else{
                    Document result = buildCompileText(context, nextComp, null);
                    current.set("extra",new Object[]{result});
                }
            }
            return root;
        }
    }

    private static Document buildCompileText(MinecraftBuildContext context,Document document,Object nextComp){
        if(nextComp != null){
            Document root = Document.newDocument();
            root.set("text","");

            if(nextComp instanceof DocumentEntry){
                root.set("extra",new Object[]{document,nextComp});
            }else if(nextComp.getClass().isArray()){
                int length = Array.getLength(nextComp);
                if(length >= 0){
                    root.set("extra",nextComp);
                    root.getDocument("extra").entries().add(0,document);
                }
            }else{
                Document result = buildCompileText(context,nextComp,null);
                root.set("extra",new Object[]{document,result});
            }

            return root;
        }
        return document;
    }
}