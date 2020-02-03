package Menus;

public class Menu {
    protected int depthLevel = -1;
    protected String name;
    protected Menu parent;
    public static Menu root = RootMenu.root;

    public Menu(String name, Menu parent) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            this.depthLevel = parent.getDepth() + 1;
        }
        MenuManager.addMenu(this);
    }

    public Menu(Menu parent) {
        this(genName(), parent);
    }

    public Menu(String name) {
        this(name, root);
    }

    public Menu() {
        this(genName(), root);
    }

    public void printParents() {
        getParent().printParents();
        System.out.println(this);
    }

    public void printFamily() {
        getParent().printParents();
        printChilds();
    }

    public void printChilds() {
        MenuManager.printChilds(this);
    }

    public Menu newMenu(String name) {
        return new Menu(name, this);
    }

    public Menu newMenu() {
        return newMenu(genName());
    }

    public Menu getParent() {
        return parent;
    }

    protected String getPrefix() {
        return getPrefix(this.depthLevel);
    }

    public static String getPrefix(int i) {
        if (i > 0) {
            return "|".repeat(i);
        } else {
            return "";
        }
    }

    public String getName() {
        return name;
    }

    public int getDepth() {
        return depthLevel;
    }

    @Override
    public String toString() {
        return getPrefix() + "Menu level " + depthLevel + ": " + name + "; Parent: " + (getParent() == null ? "null" : getParent().getName());
    }

    public static String genName() {
        return "Menu " + (MenuManager.getMenuCount() + 1);
    }

}
