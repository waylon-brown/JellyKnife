# JellyKnife
Bind your Android [Data Binding](https://developer.android.com/topic/libraries/data-binding/index.html) views to your fields with annotations. 

## Use
```java
@DataBinding public ActivityHomeBinding binding;  // Define your ViewDataBinding
@Bind public Toolbar toolbar;                     // Define your views, which are found via matching names in the ViewDataBinding
@Bind public DrawerLayout drawerLayout;
@Bind public FloatingActionButton fab;
@Bind public NavigationView navView;
@Bind public RecyclerView recyclerView;
@Bind public SwipeRefreshLayout swipeRefreshLayout;

@Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  ActivityHomeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
  
  // Binds your annotated fields to the views generated in Data Binding
  JellyKnife.bind(this);
}
```

If you don't want to make your `ViewDataBinding` a field, you can instead call

```java
JellyKnife.bind(this, binding);
```

## What is it doing?
Rather than binding the fields to the views through `R.id` magic like Butterknife, JellyKnife simply leverages the fact that if you're using Android Data Binding, that library already generates your bound Views into code for you. You've already seen this if you've accessed your View fields using things such as `TextView textView = myBinding.textView;` and `SnackBar snackBar = myBinding.innerBinding.snackBar;`. 

Even though the Data Binding library generates the View fields for you, using that library alone can still lead to a fair amount of boilerplate such as having to preface all of your View accessing in the code with `myBinding.` and `myBinding.innerBinding.` (these names are examples) like so:

```java
binding.textView.setText("Hello world!");
setSupportToolbar(binding.innerBinding.toolbar);
binding.recyclerView.setAdapter(adapter);
```

or through field setting boilerplate such as:

```java
TextView textView;
Toolbar toolbar;
RecyclerView recyclerView;
FloatingActionButton fab;

@Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  ActivityHomeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        
  textView = binding.textView;
  toolbar = binding.innerBinding.toolbar;
  recyclerView = binding.recyclerView;
  fab = binding.innerBinding.toolbar;
}
```

All JellyKnife does is find your annotations, and point those fields to the appropriate Views in the ViewDataBinding class. Even though many Views will not need to be referenced in code if using Data Binding correctly, it's still inevitable with any View that needs to be tied into the framework or is customized in any way not possible in XML.

## Requirements
* The field annotated with `@DataBinding` is a subclass of type `ViewDataBinding`
* Each field annotated with `@Bind` is a View
* Each field is public
* Each field exists within the `ViewDataBinding` with the same name 

The library will crash the app immediately and let you know if it was set up incorrectly.

## What's this solving over just using Android Data Binding + ButterKnife?
Really, just that it's more lightweight.  If you're already using Data Binding, you might find that using JellyKnife with it solves your same use cases with much less code since Data Binding is already doing a lot of heavy lifting. *This is a very small library.* 

The only difference at all to you as the developer is that the annotations don't require the view's IDs to be passed in. That, and it has less features. JellyKnife will fit your use case if you use ButterKnife purely for the @BindView annotations and nothing else, it's not up to complete feature parity with it and doesn't strive to be.
