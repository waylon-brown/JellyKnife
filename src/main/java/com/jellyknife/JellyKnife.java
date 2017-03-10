package com.jellyknife;

import android.databinding.ViewDataBinding;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class JellyKnife {

    /**
     * Used when a {@link ViewDataBinding} field of the target is annotated with {@link DataBinding}.
     * 
     * @param target
     */
    public static void bind(Object target) {
        ViewDataBinding binding = null;
        try {
            binding = getViewDataBinding(target);
        } catch (IllegalAccessException | IllegalStateException e) {
            e.printStackTrace();
        }
        bind(target, binding);
    }

    /**
     * Used when providing a {@link ViewDataBinding} instead of using the {@link DataBinding} annotation.
     * 
     * @param target
     * @param binding
     */
    public static void bind(Object target, ViewDataBinding binding) {
        try {
            if (binding != null) {
                bindFields(target, binding);
            } else {
                Timber.e("No field was annotated with DataBinding");
                return;
            }
        } catch (IllegalAccessException | IllegalStateException | ClassCastException e) {
            // Each of the thrown exceptions should cause an app crash - fail early.
            throw new RuntimeException(e);
        }
    }

    private static ViewDataBinding getViewDataBinding(Object target) throws IllegalAccessException {
        for (Field field : target.getClass().getDeclaredFields()) {
            DataBinding annotation = field.getAnnotation(DataBinding.class);
            if (annotation != null) {
                Timber.d("DataBinding annotation on field '" + field.getName() + "'.");
                try {
                    if (field.get(target) instanceof ViewDataBinding) {
                        return (ViewDataBinding)field.get(target);
                    } else if (field.get(target) == null) {
                        throw new IllegalStateException("DataBinding field '" + field.getName() + "' was null at the time of calling JellyKnife.bind().");
                    } else {
                        throw new IllegalStateException("DataBinding field '" + field.getName() + "' is not an instance of ViewDataBinding.");
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalAccessException("DataBinding field '" + field.getName() + "' isn't declared public.");
                }
            }
        }
        return null;
    }

    private static void bindFields(Object target, ViewDataBinding binding) throws IllegalAccessException {
        for (Field field : target.getClass().getDeclaredFields()) {
            Bind annotation = field.getAnnotation(Bind.class);
            if (annotation != null) {
                Timber.d("Binding annotation on field '" + field.getName() + "'.");
                bindField(field, target, binding);
            }
        }
    }

    private static void bindField(Field field, Object target, ViewDataBinding binding) throws IllegalAccessException {
        try {
            // Failing early, TODO: change
            // ClassCastException
            field.getType().asSubclass(View.class);
            // IllegalAccessException
            field.get(target);
            
            List<ViewDataBinding> viewDataBindingList = new ArrayList<>();
            viewDataBindingList.add(binding);
            View bindingView = getViewFromBinding(field.getName(), viewDataBindingList);
            
            if (bindingView != null) {
                field.set(target, bindingView);
            } else {
                throw new IllegalStateException("No View in " + binding.getClass() + " was found with the name '" + field.getName() + "'.");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Binding field '" + field.getName() + "' wasn't of type View.");
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("Binding field '" + field.getName() + "' isn't declared public.");
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
                // TODO: clean up
                try {
                    // Throws exception if isn't a ViewDataBinding
                    field.getType().asSubclass(ViewDataBinding.class);
                    viewDataBindingList.add((ViewDataBinding)field.get(binding));
                } catch (ClassCastException e) {
                    // Do nothing, isn't of type ViewDataBinding
                }
            }
        }
        if (viewDataBindingList.isEmpty()) {
            return null;
        }
        return getViewFromBinding(name, viewDataBindingList);
    }
}
