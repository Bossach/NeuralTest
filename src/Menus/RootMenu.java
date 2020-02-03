package Menus;

public class RootMenu extends Menu {
    public static Menu root = new RootMenu();

    public RootMenu() {
        this.name = "root";
        this.parent = this;
        this.depthLevel = -1;
    }

    @Override
    public void printParents() { }
}
