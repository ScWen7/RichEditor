## RichEditor

RichEditor is a rich text editor work on Android.

### Screenshots:

![](https://github.com/ScWen7/RichEditor/blob/master/screenshots/screenshots1.jpg)

Until now,it support these styles:

- Bold
- Italic
- Underline
- Strikethrough
- Quote
- Bullet
- FontSize
- Align Left
- Align Center
- Align Right
- Todos
- Insert Image

All Styles can export as HTML , as well  it also can parse HTML to native weight  then edit or display.



### Download

```groovy
implementation 'com.github.scwen7:RichEditor:1.0.1'
```

Or you can ```git clone https://github.com/ScWen7/RichEditor.git```  and import **RichEditor** as a module.

Other way,you may have different business scenarios, I'd like recommend to import `RichEditor` into your project direclty and add it as a local module dependency. 

This library has used  [Glide](https://github.com/bumptech/glide)  

```java
implementation 'com.github.bumptech.glide:glide:4.8.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.8.0'
```



### Example

In your Layout 

```Xml
 <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        >
            <com.scwen.editor.RichEditer
                android:id="@+id/editor_content"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginLeft="20dp"
                android:layout_marginTop="10dp"
                android:layout_marginRight="20dp"
                android:layout_marginBottom="20dp"
                android:paddingLeft="5dp"
                android:paddingRight="5dp"
                android:paddingBottom="5dp"/>

</ScrollView>
```

In your Activity 

`RichEditer mEditor= findViewById(R.id.editor_content)`

**FontSize**

```
mEditor.fontSize();
```

**Alignment**

```
mEditor.alignment();
```

**Bullet**

```java
mEditor.bullet();
```

**Todo**

```Java
mEditor.todo();
```

**Bold**

```java
mEditor.bold();
```

**Italic**

```Java
mEditor.italic();
```

**Underline**

```Java
mEditor.underline();
```

**Quote**

```
mEditor.quote();
```

**Insert Image**

```java
mEditor.insertImage(imagePath);
```