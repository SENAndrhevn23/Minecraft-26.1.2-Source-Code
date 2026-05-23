package net.minecraft.client.model;

public record AdultAndBabyModelPair<T extends Model<?>>(T adultModel, T babyModel) {
   public T getModel(final boolean isBaby) {
      return (T)(isBaby ? this.babyModel : this.adultModel);
   }
}
