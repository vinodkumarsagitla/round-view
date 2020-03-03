# round-view
Simple Custom Round View For TextView, Button, EditText, LinearLayout, RelativeLayout, FrameLayout, ImageView 

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  Step 2. Add the dependency
  dependencies {
	        implementation 'com.github.vinodkumarsagitla:round-view:1.0.0'
	}
  
Usage
Add your layout & view via XML

  <com.vks.RoundTextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="10dp"
                android:text="Round Text View"
                app:rv_cornerRadius="10dp"
                app:rv_strokeColor="@color/colorPrimary"
                app:rv_strokeWidth="1dp"
                tools:ignore="HardcodedText"/>

            <com.vks.RoundTextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="10dp"
                android:padding="10dp"
                android:text="Round Text View"
                app:rv_backgroundColor="@color/colorAccent"
                app:rv_cornerRadius="10dp"
                app:rv_strokeColor="@color/colorPrimary"
                app:rv_strokeWidth="1dp"
                tools:ignore="HardcodedText" />
                
                 <com.vks.RoundedImageView
                android:layout_width="140dp"
                android:layout_height="140dp"
                android:scaleType="centerCrop"
                android:src="@drawable/vks"
                app:rv_isOval="true"
                app:rv_strokeColor="@color/colorPrimaryDark"
                app:rv_strokeWidth="1dp" />
                
                 <com.vks.RoundEditText
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:padding="10dp"
                android:text="Round Edit Text"
                app:rv_cornerRadius="10dp"
                app:rv_strokeColor="@color/colorPrimary"
                app:rv_strokeWidth="1dp"
                tools:ignore="HardcodedText" />

            <com.vks.RoundTextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginLeft="10dp"
                android:padding="10dp"
                android:text="Dotted Border"
                app:rv_cornerRadius="10dp"
                app:rv_dashGap="5dp"
                app:rv_dashWidth="5dp"
                app:rv_strokeColor="@color/colorPrimary"
                app:rv_strokeWidth="2dp"
                tools:ignore="HardcodedText" />
                
                <com.vks.RoundLinearLayout
            android:layout_width="160dp"
            android:layout_height="40dp"
            android:layout_marginTop="10dp"
            android:padding="10dp"
            app:rv_backgroundColor="@color/colorAccent"
            app:rv_isRadiusHalfHeight="true"
            tools:ignore="HardcodedText" />
            
