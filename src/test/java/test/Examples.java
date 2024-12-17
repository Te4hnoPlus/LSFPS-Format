package test;

import plus.format.FormatBuilder;
import plus.format.Formatter;
import plus.format.IVarGetter;
import plus.format.MapVarProvider;

import java.util.Locale;


public class Examples {
    private static final MapVarProvider<User> PROVIDER = new MapVarProvider<>(User.class);


    public static class User{
        String name;
        int age;
        User parent;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }


    public static void exampleRegister(){
        PROVIDER.section("func")
                .reg("has-parent", (IVarGetter.Bool<User>) user -> user.parent != null)
                .reg("an-adult", (IVarGetter.Bool<User>) user -> user.age >= 18)
                .back()
                .section("extra")
                .reg("name-caps", (IVarGetter.Str<User>) user -> user.name.toUpperCase(Locale.ROOT));
    }


    public static void exampleFormat(){
        FormatBuilder<User> fmBuilder = new FormatBuilder<>(Examples.User.class)
                .setProvider(PROVIDER)
                .fixEmpty();

        fmBuilder.setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");

        Formatter<User> format = fmBuilder.build();

        Examples.User user1 = new Examples.User("Homa", 1);
        Examples.User user2 = new Examples.User("Te4hno", 5);

        user1.parent = user2;

        System.out.println(format.get(user1));
    }
}
