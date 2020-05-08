# CircularSeekBar
This project is custom circular shape SeekBar.

## Usage
To start using this component you need to:
* Add `attrs.xml` to your `res/value` folder
* Add `CircularSeekBar.kt` to your package
* Place the component to your layout
```
    <com.alex_malishev.circular_seekbar.CircularSeekBar
        android:id="@+id/roundSeekBar4"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:detectTouchInside="true"        
        app:rotateAngle="180" 
        app:startAngle="0" // the end angle must be
        app:endAngle="180" // greater than start angle
        app:progress="10"
        app:primaryColor="@color/colorAccent"
        app:textColor="@android:color/darker_gray"
        app:textProgressEnabled="true"
        app:secondaryColor="#570800FF"
        app:seekDirection="counterclockwise"
        app:secondaryProgress="50" />
```
* Add `SeekListener` if you want to
```
    seekBar.setSeekListener(object : CircularSeekBar.SeekListener{
            override fun onProgressChanged(seekBar: CircularSeekBar, progress: Long, byUser: Boolean) {
                Log.e(TAG, "Progress was changed to $progress. Did user change it? - $byUser")
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
                Log.e(TAG, "User is starting to change current value")
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
                seekBar.setPrimaryColor(R.color.colorAccent)
            }
        })
```
* You can also set properties programmatically
```
    seekBar.setPrimaryColor(R.color.colorPrimary)
    seekBar.setSecondaryColor(android.R.color.holo_red_light)
    seekBar.maxProgress = 100
    seekBar.minProgress = 0
    seekBar.progress = 50
    seekBar.secondaryProgress = 70
    seekBar.startAngle = 180f
    seekBar.endAngle = 360f
    seekBar.seekDirection = CircularSeekBar.Direction.COUNTERCLOCKWISE
    seekBar.setThumb(R.mipmap.ic_launcher_round)
```
## Example
![Example GIF](https://raw.githubusercontent.com/alexmalishev270896/circularseekbar/master/image/seekbar.gif)
* `activity_main.xml`
```
 <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.alex_malishev.circular_seekbar.CircularSeekBar
        android:id="@+id/roundSeekBar"
        style="@style/SeekBarStyle1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        app:detectTouchInside="true"
        app:endAngle="350"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:rotateAngle="90"
        app:startAngle="10" />


</androidx.constraintlayout.widget.ConstraintLayout>
````
* `MainActivity.kt`
```
class MainActivity : AppCompatActivity() {

    companion object{
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roundSeekBar.setSeekListener(object : CircularSeekBar.SeekListener{
            override fun onProgressChanged(seekBar: CircularSeekBar, progress: Long, byUser: Boolean) {
                Log.e(TAG, "Progress was changed to $progress. Did user change it? - $byUser")
            }

            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {
                seekBar.setPrimaryColor(R.color.colorPrimary)
                seekBar.setTextColor(R.color.colorPrimary)
                Log.e(TAG, "User is starting to change current value")
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {
                Log.e(TAG, "User stopped to change current value")
                seekBar.setPrimaryColor(R.color.colorAccent)
                seekBar.setTextColor(android.R.color.darker_gray)
            }
        })
    }
}
```

## Contact
Feel free to contact me via
* Telegram - [@AlexMali](https://t.me/AlexMali)
* E-mail - [alex.malishev270896@gmail.com](mailto:alex.malishev270896@gmail.com)
