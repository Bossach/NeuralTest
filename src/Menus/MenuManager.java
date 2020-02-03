package Menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuManager {
    private final static int MENU_WARNING_LIMIT = 64;   //количество меню при превышении которого будет появляться предупреждение

    private static List<Menu> menusList = new ArrayList<>();    //all menus are stored there

    protected static void addMenu(Menu newMenu) {   //all menus automatic adds here in constructor
            menusList.add(newMenu);
            if (menusList.size() > MENU_WARNING_LIMIT) {
                System.err.println("MenuManager.java Warning: set limit of menus exceeded. " + menusList.size() + " of " + MENU_WARNING_LIMIT);
            }
    }

    public static void printChilds() {     //prints all menus with hierarchy
        printChilds(Menu.root);
    }

    public static void printChilds(Menu baseMenu) {  //prints menu and all his children
        if (baseMenu != Menu.root) System.out.println(baseMenu);
        for (Menu menu : menusList) {
            if (menu != Menu.root && menu.getParent() == baseMenu) {
                printChilds(menu);
            }
        }
    }

    public static void printChilds(String menuName) {  //prints menu and all his children
        printChilds(getFromName(menuName));
    }

    public static void printParents (Menu child) {
        if(child != Menu.root) {
            child.printParents();
        }
    }

    public static void printParents (String menuName) {
        printParents(getFromName(menuName));
    }

    public static Menu getFromName (String menuName) {
        List<Menu> list = new ArrayList<>();
        for (Menu menu : menusList) {
            if (menu.getName().equalsIgnoreCase(menuName)) {
                list.add(menu);
            }
        }
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {

            System.out.println("Found " + list.size() + " menus with name '" + menuName + "'.");
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + ": " + list.get(i));
            }
            System.out.println("Which of them? (type [1 - " + list.size() + "] or [0] to nothing)");
            Scanner scan = new Scanner(System.in);
            int n = -1;
            while (true) {
                System.out.print(":");
                try {
                    n = Integer.parseInt(scan.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("invalid number");
                    continue;
                }
                if (n > 0 && n <= list.size()) {
                    return list.get(n - 1);
                } else if (n == 0) {
                    return Menu.root;
                } else {
                    System.out.println("incorrect number");
                }
            }

        } else {
            System.out.println("Menu '" + menuName + "' not found.");
            return Menu.root;
        }
    }

    public static int getMenuCount() {
        return menusList.size() - 1;
    }
}
