/*
 *  Copyright 2017 Tua Rua Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tuarua;

import android.content.Intent;

import com.adobe.air.ActivityResultCallback;
import com.adobe.air.AndroidActivityWrapper;
import com.adobe.air.StateChangeCallback;
import com.tuarua.frekotlin.FreKotlinContext;
import com.tuarua.frekotlin.FreKotlinController;

//This file must remain as Java
public class FreKotlinExampleContext extends FreKotlinContext implements ActivityResultCallback, StateChangeCallback {
    private AndroidActivityWrapper aaw;

    public FreKotlinExampleContext(String name, FreKotlinController controller, String[] functions) {
        super(name, controller, functions);
        aaw = AndroidActivityWrapper.GetAndroidActivityWrapper();
        aaw.addActivityResultListener(this);
        aaw.addActivityStateChangeListner(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onActivityStateChanged(AndroidActivityWrapper.ActivityState activityState) {
        super.onActivityStateChanged(activityState);
        switch (activityState){
            case STARTED:
                break;
            case RESTARTED:
                break;
            case RESUMED:
                break;
            case PAUSED:
                break;
            case STOPPED:
                break;
            case DESTROYED:
                break;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (aaw != null) {
            aaw.removeActivityResultListener(this);
            aaw.removeActivityStateChangeListner(this);
            aaw = null;
        }
    }
}