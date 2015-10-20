public class Test {
    private static final byte ALPHA = (Character.LOWERCASE_LETTER|Character.UPPERCASE_LETTER);
    public static void main(String[] args) {
        System.out.format("%d\n", Character.getType('a'));
        System.out.format("%d\n", Character.getType('A'));
        System.out.format("%d\n", Character.getType('#'));
        System.out.format("%d\n", Character.getType('('));
    }
}
