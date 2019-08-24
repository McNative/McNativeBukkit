/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 04.08.19 10:44
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.common.text;

import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.text.event.ClickAction;
import org.mcnative.common.text.format.TextColor;
import org.mcnative.common.text.format.TextStyle;
import org.mcnative.common.text.variable.KeyComponent;
import org.mcnative.common.text.variable.Variable;
import org.mcnative.common.text.variable.VariableSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text extends AbstractChatComponent<Text> implements TextComponent<Text>{

    public static final char FORMAT_CHAR = '\u00A7';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

    private String text;

    public Text(){
        this(null);
    }

    public Text(String text){
        this(text,TextColor.WHITE);
    }

    public Text(String text, TextColor color) {
        super(color);
        this.text = text;
    }

    public Text(String text,TextColor color, Collection<TextStyle> styling) {
        super(color, styling);
        this.text = text;
    }

    @Override
    public String getText() {
        return text!=null?text:"";
    }

    @Override
    public Text setText(String text) {
        this.text = text;
        return this;
    }


    @Override
    public void toPlainText(StringBuilder builder) {

    }

    @Override
    public void compile(StringBuilder builder) {

    }






    public static TextBuilder build(){
        return new TextBuilder();
    }

    public static TextComponent of(String content){
        return new Text(content);//translate
    }

    public static TextComponent of(String content, TextColor color){
        return new Text(content,color);
    }

    public static TextComponent of(String content, TextStyle... styles){
        return new Text(content,null, Arrays.asList(styles));
    }

    public static TextComponent of(String content, TextColor color, TextStyle... styles){
        return new Text(content,color, Arrays.asList(styles));
    }

    public static MessageComponentSet of(TextComponent... components){
        return new MessageComponentSet(Arrays.asList(components));
    }


    public static KeyComponent ofKey(String messageKey){
        return new KeyComponent(messageKey);
    }

    public static KeyComponent ofKey(String messageKey, Variable... variables){
        return ofKey(messageKey,new VariableSet(Arrays.asList(variables)));
    }

    public static KeyComponent ofKey(String messageKey, VariableSet variables){
        return new KeyComponent(messageKey,variables);
    }

    private static final Pattern URL_PATTERN = Pattern.compile("([!?]\\[(.*?)\\](\\s*)\\(.*?\\))|(&[0-9-aA-fF-lL-oO-Rr])+|((https|http):\\/\\/|www\\.)[a-zA-Z0-9]+[a-zA-Z0-9]\\.[^\\s]{2,}");

    /*
        &5

        ![Test](https://test.com|Gehe zur Webseite)
        ![Test](run://game join 10999)
        ![Test](server://lobby-1)
        ![Test](chat://Das ist eine vorbereitete Nachricht)

        ?[Test](MyCoolHoverMessage)


        ![Test](chat://Das ist eine vorbereitete Nachricht)



        #[key](Click|Hover)  -> Keybind
        #[entity|objective](Click|Hover) -> Score


        ![&5[&7Join&5]](run://game join 10999)



        (&[0-9-aA-fF-lL-oO-Rr]) - colors

        [!?](\[(.*?)\](\s*)\(.*?\)) - markup


        ([!?]\[(.*?)\](\s*)\(.*?\))|((https|http):\/\/)


        ([!?]\[(.*?)\](\s*)\(.*?\))|((https|http):\/\/)[a-zA-Z0-9]+[a-zA-Z0-9]\.[^\s]{2,} - Url only with https or http

        ([!?]\[(.*?)\](\s*)\(.*?\))|((https|http):\/\/|www\.)[a-zA-Z0-9]+[a-zA-Z0-9]\.[^\s]{2,} - Urls with https, http and www

        ([!?]\[(.*?)\](\s*)\(.*?\))|((https|http):\/\/)*[a-zA-Z0-9]+[a-zA-Z0-9]\.[^\s]{2,} -> Domain without http or https


        ([!?]\[(.*?)\](\s*)\(.*?\))|(&[0-9-aA-fF-lL-oO-Rr])|((https|http):\/\/|www\.)[a-zA-Z0-9]+[a-zA-Z0-9]\.[^\s]{2,} Markup, urls and colors




        Hallo du, |&5gehe zu |https://link.com||
     */

    public static Collection<MessageComponent> ofPlainText(String text,Character alternateChar){
        Collection<MessageComponent> components = new ArrayList<>();
        MessageComponent last;

        char[] content = text.toCharArray();
        Matcher markups = URL_PATTERN.matcher(text);

        int lastIndex;

        int lastColorIndex = 0;
        while(markups.find()){
            char type = content[markups.start()];


            if(type == '&'){
                TextComponent  component = new Text();
                for (int index = markups.start()+1; index < markups.end(); index+=2) {
                    char code = content[index];
                    TextColor color = TextColor.fromCode(code);
                    if(color != null) component.setColor(color);
                    else{
                        TextStyle style = TextStyle.fromCode(code);
                        if(style != null) component.addStyle(style);
                    }
                }
                components.add(component);
            }else if(type == '!'){

            }else if(type == '?'){

            }else{

            }
        }


        //text.substring()

        /*
        for(int i = 0; i < content.length; i++) {
            if(c == FORMAT_CHAR || (alternateChar != null && alternateChar == c)){

            }
        }
         */
        return components;
    }

    public static void main(String[] args){
//Hallo, ![&7Clic$k mich](https//:test.com:test) asssasdsa [test) test https://test.com www.test.com ?[&7Clic$k mich](https//:test.com:test)  lol.ch
        Matcher m = URL_PATTERN.matcher("![&5[&7Join&5]](run://game join 10999)");

        long start = System.currentTimeMillis();


        while (m.find()) {
            System.out.println("Found: "+m.group()+" | "+m.start()+" -> "+m.end());
        }

        start = System.currentTimeMillis()-start;
        System.out.println(start+"ms");
    }

    public static TextComponent replace(String variableName, TextComponent source, TextComponent replacement){

        return null;
    }



    public static String translateAlternateColorCodes(char alternateChar,String text){
        char[] content = text.toCharArray();
        for(int i = 0; i < text.toCharArray().length; i++) {
            if(content[i] == alternateChar && ALL_CODES.indexOf(content[i]+1) > -1){
                content[i] = FORMAT_CHAR;
            }
        }
        return new String(content);
    }



    public static void test(OnlineMinecraftPlayer player){
        MessageComponent accept = Text.ofKey("friend.request.accept").setClickEvent(ClickAction.RUN_COMMAND,"/friend accept player");
        MessageComponent deny = Text.ofKey("friend.request.deny").setClickEvent(ClickAction.RUN_COMMAND,"/friend deny player");

        KeyComponent text = Text.ofKey("friend.request")
                .addVariable("player",null)
                .addVariable("friend",null)
                .addVariable("accept",accept)
                .addVariable("deny",deny);

       // player.sendMessage(text);
    }

}
