package com.jellyknife;

import android.databinding.ViewDataBinding;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Binds Views provided in generated {@link ViewDataBinding} classes to View fields annotated
 * with {@link Bind}.
 * 
 * The {@link ViewDataBinding} class can be provided either with 
 *     1) The {@link DataBinding} annotation and using {@link #bind(Object)}.
 *     2) By just passing it in with {@link #bind(Object, ViewDataBinding)}.
 */
public class JellyKnife {

    /**
     * Used when a {@link ViewDataBinding} field of the target is annotated with {@link DataBinding}.
     * 
     * @param target
     */
    public static void bind(Object target) {
        ViewDataBinding binding = getViewDataBinding(target);
        if (binding != null) {
            bind(target, binding);
        } else {
            throw new IllegalStateException("No field found in the class " + target.getClass().getName() +
                    " with the DataBinding annotation, did you mean to call bind(Object target, ViewDataBinding binding)?");
        } 
    }

    /**
     * Used when providing a {@link ViewDataBinding} instead of using the {@link DataBinding} annotation.
     * 
     * @param target
     * @param binding
     */
    public static void bind(Object target, ViewDataBinding binding) {
        if (binding != null) {
            bindFields(target, binding);
        } else {
            Timber.e("No field was annotated with DataBinding");
            return;
        }
    }

    private static ViewDataBinding getViewDataBinding(Object target) {
        for (Field field : target.getClass().getDeclaredFields()) {
            DataBinding annotation = field.getAnnotation(DataBinding.class);
            if (annotation != null) {
                try {
                    if (field.get(target) instanceof ViewDataBinding) {
                        return (ViewDataBinding)field.get(target);
                    } else if (field.get(target) == null) {
                        throw new IllegalStateException("DataBinding field '" + field.getName() + "' was null at the time of calling JellyKnife.bind().");
                    } else {
                        throw new IllegalStateException("DataBinding field '" + field.getName() + "' is not an instance of ViewDataBinding.");
                    }
                } catch (IllegalAccessException e) {
                    // Rethrow a Runtime exception
                    throw new IllegalStateException("DataBinding field '" + field.getName() + "' isn't declared public.");
                }
            }
        }
        return null;
    }

    private static void bindFields(Object target, ViewDataBinding binding) {
        for (Field field : target.getClass().getDeclaredFields()) {
            Bind annotation = field.getAnnotation(Bind.class);
            if (annotation != null) {
                bindField(field, target, binding);
            }
        }
    }

    private static void bindField(Field field, Object target, ViewDataBinding binding) {
        try {
            // Field is of type View
            if (View.class.isAssignableFrom(field.getType())) {
                List<ViewDataBinding> viewDataBindingList = new ArrayList<>();
                viewDataBindingList.add(binding);
                View bindingView = getViewFromBinding(field.getName(), viewDataBindingList);

                if (bindingView != null) {
                    field.set(target, bindingView);
                } else {
                    throw new IllegalStateException("No View in " + binding.getClass() + " was found with the name '" + field.getName() + "'.");
                }
            } else {
                throw new IllegalStateException("Binding field '" + field.getName() + "' wasn't of type View.");
            }
        } catch (IllegalAccessException e) {
            // Rethrow a Runtime exception
            throw new IllegalStateException("Binding field '" + field.getName() + "' isn't declared public.");
        } 
    }

    /**
     * Check if any of the fields matches the field name, otherwise recursively perform the same check on any
     * inner {@link ViewDataBinding}s.
     * 
     * @param name
     * @param viewDataBindingList is a list of ViewDataBindings, initially starting as the main binding but has any inner
     *                            bindings added to the list.
     * @return
     * @throws IllegalAccessException
     */
    private static View getViewFromBinding(String name, List<ViewDataBinding> viewDataBindingList) throws IllegalAccessException {
        if (viewDataBindingList == null || viewDataBindingList.isEmpty()) {
            return null;
        }
        ViewDataBinding binding = viewDataBindingList.get(0);
        viewDataBindingList.remove(0);
        
        // Iterate through fields of the data binding
        for (Field field : binding.getClass().getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return (View)field.get(binding);
            } else {
                // Field is of type ViewDataBinding
                if (ViewDataBinding.class.isAssignableFrom(field.getType())) {
                    viewDataBindingList.add((ViewDataBinding)field.get(binding));
                }
            }
        }
        if (viewDataBindingList.isEmpty()) {
            return null;
        }
        return getViewFromBinding(name, viewDataBindingList);
    }
}
