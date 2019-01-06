package cn.gavinliu.notificationbox.baidutts;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class ParcelableCartoon implements Parcelable {

    private Cartoon cartoon;
    private static final String MSG = "MESSAGE";

    public ParcelableCartoon(Cartoon cartoon) {
        Log.i(MSG, "ParcelableCartoon::ParcelableCartoon@Cartoon");
        this.cartoon = cartoon;
    }

    // 将对象写入Parcel容器中去
    // 完成对对象的序列化

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.i(MSG, "ParcelableCartoon::writeToParcel");
        dest.writeString(cartoon.getName());
        dest.writeString(cartoon.getCreator());
        dest.writeParcelable(cartoon.getFigure(), PARCELABLE_WRITE_RETURN_VALUE);
    }


    // 完成对序列化的对象反序列化
    public static final Parcelable.Creator<ParcelableCartoon> CREATOR = new Parcelable.Creator<ParcelableCartoon>() {
        // 从Parcel容器中获取序列化的对象，并将其反序列化，得到该对象的实例

        /**
         * Create a new instance of the Parcelable class, instantiating it
         * from the given Parcel whose data had previously been written by
         * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
         *
         * @param source The Parcel to read the object's data from.
         * @return Returns a new instance of the Parcelable class.
         */
        @Override
        public ParcelableCartoon createFromParcel(Parcel source) {
            Log.i(MSG, "ParcelableCartoon::Parcelable.Creator::createFromParcel");
            return new ParcelableCartoon(source);
        }

        @Override
        public ParcelableCartoon[] newArray(int size) {
            Log.i(MSG, "ParcelableCartoon::Parcelable.Creator::newArray");
            return new ParcelableCartoon[size];
        }

    };

    public ParcelableCartoon(Parcel in) {
        Log.i(MSG, "ParcelableCartoon::ParcelableCartoon@Parcel");
        cartoon = new Cartoon();
        String name = in.readString();

        cartoon.setName(name);
        cartoon.setCreator(in.readString());
        cartoon.setFigure((Bitmap) in.readParcelable(Bitmap.class.getClassLoader()));
    }

    public Cartoon getCartoon() {
        Log.i(MSG, "ParcelableCartoon::getCartoon");
        return cartoon;
    }

    @Override
    public int describeContents() {
        Log.i(MSG, "ParcelableCartoon::describeContents");
        return 0;
    }





    public class Cartoon {
        private Bitmap figure;
        private String name;
        private String creator;

        public Bitmap getFigure() {
            return figure;
        }

        public void setFigure(Bitmap figure) {
            this.figure = figure;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
    }
}


