package co.hoppen.filter.face01;

import android.graphics.Bitmap;

/**
 * Created by YangJianHui on 2024/4/8.
 */
public abstract class FaceZoFilter {

   private Bitmap originalImage;

   private Bitmap filterPartsImages;

   protected FaceZoFilter() {
   }

   public abstract void onFilter(FilterFaceZoInfoResult filterFaceInfoResult) throws Exception;

   public void setImage(Bitmap originalImage,Bitmap filterPartsImages){
      this.originalImage = originalImage;
      this.filterPartsImages = filterPartsImages;
   }

   public Bitmap getOriginalImage() {
      return originalImage;
   }

   public Bitmap getFilterPartsImages() {
      return filterPartsImages;
   }

   public void recycleImages(){
      if (originalImage!=null && !originalImage.isRecycled()){
         originalImage.recycle();
         originalImage = null;
      }
      if (filterPartsImages!=null && !filterPartsImages.isRecycled()){
         filterPartsImages.recycle();
         filterPartsImages = null;
      }
   }

}
