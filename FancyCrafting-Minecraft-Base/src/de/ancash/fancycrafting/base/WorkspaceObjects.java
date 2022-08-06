package de.ancash.fancycrafting.base;

import java.util.List;

import de.ancash.minecraft.IItemStack;

public class WorkspaceObjects {

	private IItemStack backItem;
	private IItemStack closeItem;
	private IItemStack prevItem;
	private IItemStack nextItem;
	private IItemStack invalidItem;
	private IItemStack validItem;
	private IItemStack backgroundItem;
	private IItemStack shapelessItem;
	private IItemStack shapedItem;
	private IItemStack saveItem;
	private IItemStack editItem;
	private IItemStack deleteItem;
	private IItemStack quickCraftingItem;
	private IItemStack createNormalRecipeItem;
	private IItemStack createRandomRecipeItem;
	private IItemStack manageRandomResultsItem;
	private IItemStack viewRandomResultsItem;
	private IItemStack manageIngredientsItem;
	private IItemStack viewIngredientsItem;
	private IItemStack manageRandomInvalidResultItem;
	private IItemStack inputRecipeNameRightItem;
	private IItemStack inputRecipeNameLeftItem;
	private IItemStack manageRecipeNameItem;
	private String createRecipeTitle;
	private String ingredientsInputTitle;
	private String manageResultTitle;
	private String customRecipesTitle;
	private String viewRecipeTitle;
	private String editRecipeTitle;
	private String manageProbabilitiesTitle;
	private String manageRandomResultsFormat;
	private String manageIngredientsIdFormat;
	private String viewIngredientsIdFormat;
	private String viewRandomResultsFormat;
	private String inputRecipeNameTitle;
	private String manageVanillaRecipesTitle;
	private List<String> manageProbabilityFooter;
	private List<String> manageProbabilityHeader;
	private List<String> backCommands;

	public IItemStack getCreateNormalRecipeItem() {
		return createNormalRecipeItem;
	}

	WorkspaceObjects setCreateNormalRecipeItem(IItemStack createNormalRecipeItem) {
		this.createNormalRecipeItem = createNormalRecipeItem;
		return this;
	}

	public IItemStack getCreateRandomRecipeItem() {
		return createRandomRecipeItem;
	}

	WorkspaceObjects setCreateRandomRecipeItem(IItemStack createRandomRecipeItem) {
		this.createRandomRecipeItem = createRandomRecipeItem;
		return this;
	}

	public IItemStack getBackItem() {
		return backItem;
	}

	WorkspaceObjects setBackItem(IItemStack backItem) {
		this.backItem = backItem;
		return this;
	}

	public IItemStack getCloseItem() {
		return closeItem;
	}

	WorkspaceObjects setCloseItem(IItemStack closeItem) {
		this.closeItem = closeItem;
		return this;
	}

	public IItemStack getPrevItem() {
		return prevItem;
	}

	WorkspaceObjects setPrevItem(IItemStack prevItem) {
		this.prevItem = prevItem;
		return this;
	}

	public IItemStack getNextItem() {
		return nextItem;
	}

	WorkspaceObjects setNextItem(IItemStack nextItem) {
		this.nextItem = nextItem;
		return this;
	}

	public IItemStack getInvalidItem() {
		return invalidItem;
	}

	WorkspaceObjects setInvalidItem(IItemStack invalidItem) {
		this.invalidItem = invalidItem;
		return this;
	}

	public IItemStack getValidItem() {
		return validItem;
	}

	WorkspaceObjects setValidItem(IItemStack validItem) {
		this.validItem = validItem;
		return this;
	}

	public IItemStack getBackgroundItem() {
		return backgroundItem;
	}

	WorkspaceObjects setBackgroundItem(IItemStack backgroundItem) {
		this.backgroundItem = backgroundItem;
		return this;
	}

	public IItemStack getShapelessItem() {
		return shapelessItem;
	}

	WorkspaceObjects setShapelessItem(IItemStack shapeless) {
		this.shapelessItem = shapeless;
		return this;
	}

	public IItemStack getShapedItem() {
		return shapedItem;
	}

	WorkspaceObjects setShapedItem(IItemStack shapedItem) {
		this.shapedItem = shapedItem;
		return this;
	}

	public IItemStack getSaveItem() {
		return saveItem;
	}

	WorkspaceObjects setSaveItem(IItemStack saveItem) {
		this.saveItem = saveItem;
		return this;
	}

	public IItemStack getEditItem() {
		return editItem;
	}

	WorkspaceObjects setEditItem(IItemStack editItem) {
		this.editItem = editItem;
		return this;
	}

	public IItemStack getDeleteItem() {
		return deleteItem;
	}

	WorkspaceObjects setDeleteItem(IItemStack deleteItem) {
		this.deleteItem = deleteItem;
		return this;
	}

	public IItemStack getQuickCraftingItem() {
		return quickCraftingItem;
	}

	WorkspaceObjects setQuickCraftingItem(IItemStack quickCraftingItem) {
		this.quickCraftingItem = quickCraftingItem;
		return this;
	}

	public String getCreateRecipeTitle() {
		return createRecipeTitle;
	}

	WorkspaceObjects setCreateRecipeTitle(String createRecipeTitle) {
		this.createRecipeTitle = createRecipeTitle;
		return this;
	}

	public String getCustomRecipesTitle() {
		return customRecipesTitle;
	}

	WorkspaceObjects setCustomRecipesTitle(String customRecipesTitle) {
		this.customRecipesTitle = customRecipesTitle;
		return this;
	}

	public String getViewRecipeTitle() {
		return viewRecipeTitle;
	}

	WorkspaceObjects setViewRecipeTitle(String viewRecipeTitle) {
		this.viewRecipeTitle = viewRecipeTitle;
		return this;
	}

	public String getEditRecipeTitle() {
		return editRecipeTitle;
	}

	WorkspaceObjects setEditRecipeTitle(String editRecipeTitle) {
		this.editRecipeTitle = editRecipeTitle;
		return this;
	}

	public List<String> getBackCommands() {
		return backCommands;
	}

	WorkspaceObjects setBackCommands(List<String> backCommands) {
		this.backCommands = backCommands;
		return this;
	}

	public IItemStack getManageRandomResultsItem() {
		return manageRandomResultsItem;
	}

	WorkspaceObjects setManageRandomResultsItem(IItemStack manageRandomResultsItem) {
		this.manageRandomResultsItem = manageRandomResultsItem;
		return this;
	}

	public String getManageRandomResultsFormat() {
		return manageRandomResultsFormat;
	}

	WorkspaceObjects setManageRandomResultsFormat(String manageRandomResultsFormat) {
		this.manageRandomResultsFormat = manageRandomResultsFormat;
		return this;
	}

	public IItemStack getManageIngredientsItem() {
		return manageIngredientsItem;
	}

	WorkspaceObjects setManageIngredientsItem(IItemStack manageRandomIngredientsItem) {
		this.manageIngredientsItem = manageRandomIngredientsItem;
		return this;
	}

	public String getManageIngredientsIdFormat() {
		return manageIngredientsIdFormat;
	}

	WorkspaceObjects setManageIngredientsIdFormat(String manageRandomIngredientsIdFormat) {
		this.manageIngredientsIdFormat = manageRandomIngredientsIdFormat;
		return this;
	}

	public String getIngredientsInputTitle() {
		return ingredientsInputTitle;
	}

	WorkspaceObjects setIngredientsInputTitle(String inputIngredientsTitle) {
		this.ingredientsInputTitle = inputIngredientsTitle;
		return this;
	}

	public String getManageResultTitle() {
		return manageResultTitle;
	}

	WorkspaceObjects setManageResultTitle(String manageResultTitle) {
		this.manageResultTitle = manageResultTitle;
		return this;
	}

	public List<String> getManageProbabilityFooter() {
		return manageProbabilityFooter;
	}

	WorkspaceObjects setManageProbabilityFooter(List<String> manageProbabilityFooter) {
		this.manageProbabilityFooter = manageProbabilityFooter;
		return this;
	}

	public List<String> getManageProbabilityHeader() {
		return manageProbabilityHeader;
	}

	WorkspaceObjects setManageProbabilityHeader(List<String> manageProbabilityHeader) {
		this.manageProbabilityHeader = manageProbabilityHeader;
		return this;
	}

	public String getManageProbabilitiesTitle() {
		return manageProbabilitiesTitle;
	}

	WorkspaceObjects setManageProbabilitiesTitle(String manageProbabilitiesTitle) {
		this.manageProbabilitiesTitle = manageProbabilitiesTitle;
		return this;
	}

	public IItemStack getManageRandomInvalidResultItem() {
		return manageRandomInvalidResultItem;
	}

	WorkspaceObjects setManageRandomInvalidResultItem(IItemStack manageRandomInvalidResultItem) {
		this.manageRandomInvalidResultItem = manageRandomInvalidResultItem;
		return this;
	}

	public IItemStack getInputRecipeNameLeftItem() {
		return inputRecipeNameLeftItem;
	}

	WorkspaceObjects setInputRecipeNameLeftItem(IItemStack inputRecipeNameLeft) {
		this.inputRecipeNameLeftItem = inputRecipeNameLeft;
		return this;
	}

	public IItemStack getInputRecipeNameRightItem() {
		return inputRecipeNameRightItem;
	}

	WorkspaceObjects setInputRecipeNameRightItem(IItemStack inputRecipeNameRight) {
		this.inputRecipeNameRightItem = inputRecipeNameRight;
		return this;
	}

	public String getInputRecipeNameTitle() {
		return inputRecipeNameTitle;
	}

	WorkspaceObjects setInputRecipeNameTitle(String inputRecipeNameTitle) {
		this.inputRecipeNameTitle = inputRecipeNameTitle;
		return this;
	}

	public IItemStack getManageRecipeNameItem() {
		return manageRecipeNameItem;
	}

	WorkspaceObjects setManageRecipeNameItem(IItemStack manageRecipeName) {
		this.manageRecipeNameItem = manageRecipeName;
		return this;
	}

	public IItemStack getViewIngredientsItem() {
		return viewIngredientsItem;
	}

	WorkspaceObjects setViewIngredientsItem(IItemStack viewIngredientsItem) {
		this.viewIngredientsItem = viewIngredientsItem;
		return this;
	}

	public String getViewIngredientsIdFormat() {
		return viewIngredientsIdFormat;
	}

	WorkspaceObjects setViewIngredientsIdFormat(String viewIngredientsIdFormat) {
		this.viewIngredientsIdFormat = viewIngredientsIdFormat;
		return this;
	}

	public IItemStack getViewRandomResultsItem() {
		return viewRandomResultsItem;
	}

	WorkspaceObjects setViewRandomResultsItem(IItemStack viewRandomResultsItem) {
		this.viewRandomResultsItem = viewRandomResultsItem;
		return this;
	}

	public String getViewRandomResultsFormat() {
		return viewRandomResultsFormat;
	}

	WorkspaceObjects setViewRandomResultsFormat(String viewRandomResultsFormat) {
		this.viewRandomResultsFormat = viewRandomResultsFormat;
		return this;
	}

	public String getManageVanillaRecipesTitle() {
		return manageVanillaRecipesTitle;
	}

	WorkspaceObjects setManageVanillaRecipesTitle(String manageVanillaRecipesTitle) {
		this.manageVanillaRecipesTitle = manageVanillaRecipesTitle;
		return this;
	}
}