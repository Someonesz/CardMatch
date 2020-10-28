package top.someones.cardmatch.ui;

import androidx.lifecycle.LiveData;
import top.someones.cardmatch.entity.Mod;

public class ModLiveData extends LiveData<Mod[]> {

    private static final ModLiveData mModLiveData = new ModLiveData();

    private ModLiveData() {
    }

    public static ModLiveData getLiveData() {
        return mModLiveData;
    }

    @Override
    public void postValue(Mod[] value) {
        super.postValue(value);
    }

    @Override
    public void setValue(Mod[] value) {
        super.setValue(value);
    }

}
