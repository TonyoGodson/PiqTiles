package com.godston.emojitiles;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class obj extends AppCompatActivity {
  TextView splashTV1;
  public void testing() {
      final Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
      splashTV1.setAnimation(animation);

      ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
              splashTV1,
              PropertyValuesHolder.ofFloat("scaleX", 1.2f),
              PropertyValuesHolder.ofFloat("scaleY", 1.2f)
      );
      objectAnimator.setDuration(500);
      objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
      objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
      objectAnimator.start();
  }
}
