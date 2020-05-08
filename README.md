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
    roundSeekBar.setSeekListener(object : CircularSeekBar.SeekListener{
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
        roundSeekBar.setPrimaryColor(R.color.colorPrimary)
        roundSeekBar.setSecondaryColor(android.R.color.holo_red_light)
        roundSeekBar.maxProgress = 100
        roundSeekBar.minProgress = 0
        roundSeekBar.progress = 50
        roundSeekBar.secondaryProgress = 70
        roundSeekBar.startAngle = 180f
        roundSeekBar.endAngle = 360f
        roundSeekBar.seekDirection = CircularSeekBar.Direction.COUNTERCLOCKWISE
        roundSeekBar.setThumb(R.mipmap.ic_launcher_round)
```
## Example
![Example GIF](https://drive.google.com/u/0/uc?id=16U1IQchxXvvzMEQ89dkgg0C6ZA3GqFFf&export=download)
```
 
````

## Contact
Feel free to contact me via
* Telegram - [@AlexMali](https://t.me/AlexMali)
* E-mail - [alex.malishev270896@gmail.com](mailto:alex.malishev270896@gmail.com)
