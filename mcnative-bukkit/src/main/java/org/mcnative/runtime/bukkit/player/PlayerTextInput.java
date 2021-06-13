package org.mcnative.runtime.bukkit.player;

import org.mcnative.runtime.api.player.input.PlayerTextInputValidator;
import org.mcnative.runtime.api.text.components.MessageComponent;

import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerTextInput<T> {

    private final Consumer<T> callback;
    private final Function<String, T> converter;
    private final PlayerTextInputValidator[] inputValidators;

    public PlayerTextInput(Consumer<T> callback, Function<String, T> converter, PlayerTextInputValidator[] inputValidators) {
        this.callback = callback;
        this.converter = converter;
        this.inputValidators = inputValidators;
    }

    @SuppressWarnings("unchecked")
    public void callCallback(String input) {
        if(this.converter != null) {
            T value = this.converter.apply(input);
            this.callback.accept(value);
        } else {
            this.callback.accept((T) input);
        }
    }

    public MessageComponent<?> validate(String input) {
        for (PlayerTextInputValidator validator : this.inputValidators) {
            if(!validator.isValid(input)) {
                return validator.getErrorMessage();
            }
        }
        return null;
    }
}
