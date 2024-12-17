import plus.format.FormatBuilder;
import plus.format.Formatter;
import plus.format.IVarGetter;
import plus.format.MapVarProvider;

public class TestFm {
    public static MapVarProvider<User> PRO = new MapVarProvider<User>(User.class){
        @Override
        protected void init() {
            reg("name", (IVarGetter.Str<User>) user -> user.name);
            //reg("age" , (IVarGetter.Int<User>) user -> user.age);
            reg("parent", new IVarGetter<User>() {
                public Object get(User user) {
                    return user.parent;
                }
                public Class<?> getType() {
                    return User.class;
                }
            });
        }
    };


    public static void main(String[] args) {
        FormatBuilder<User> fmBuilder = new FormatBuilder<>(User.class)
                .setProvider(PRO);

        fmBuilder.setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");

        Formatter<User> format = fmBuilder.build();

        User user1 = new User("AX", 1);
        User user2 = new User("AZ", 5);

        user1.parent = user2;

        System.out.println(format.get(user1));
    }


    public static class User{
        String name;
        int age;
        User parent;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}