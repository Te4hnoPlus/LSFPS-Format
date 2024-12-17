package test;

import plus.format.FormatBuilder;

import plus.format.Formatter;
import plus.format.IVarGetter;
import plus.format.MapVarProvider;
import test.Examples.User;


public class TestFm {
    public static MapVarProvider<User> PRO = new MapVarProvider<User>(User.class){
        @Override
        protected void init() {
            reg("name", (IVarGetter.Str<Examples.User>) user -> user.name);
            //reg("age" , (IVarGetter.Int<User>) user -> user.age);
            reg("parent", new IVarGetter<Examples.User>() {
                public Object get(User user) {
                    return user.parent;
                }
                public Class<?> getType() {
                    return Examples.User.class;
                }
            });
        }
    };


    public static void main(String[] args) {
        FormatBuilder<Examples.User> fmBuilder = new FormatBuilder<>(Examples.User.class)
                .setProvider(PRO);

        fmBuilder.setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");

        Formatter<User> format = fmBuilder.build();

        Examples.User user1 = new Examples.User("Homa", 1);
        Examples.User user2 = new Examples.User("Te4hno", 5);

        user1.parent = user2;

        System.out.println(format.get(user1));
    }
}