[ ![Download](https://api.bintray.com/packages/iammehedi/Maven/online.devliving%3Arxfirebase/images/download.svg) ](https://bintray.com/iammehedi/Maven/online.devliving%3Arxfirebase/_latestVersion)
# RxFirebase
A simple library that provides utilities to use Firebase SDK in a reactive fashion. Currently supports RxJava 1, version for RxJava 2 is coming soon!


## Setup
### Maven
```xml
<dependency>
  <groupId>online.devliving</groupId>
  <artifactId>rxfirebaselib</artifactId>
  <version>LATEST_VERSION</version>
  <type>pom</type>
</dependency>
```
### Gradle
```xml
compile 'online.devliving:rxfirebaselib:LATEST_VERSION'
```
## Usage
Turn a `Task` into an `Observable` using `RxGMSTask` utility
```java
public Observable<Void> saveUser(FirebaseUser user){
    return getUserRef()
            .flatMap(userRef -> {
                String username = usernameFromEmail(user.getEmail());
                User dbUser = new User(username, user.getEmail());

                return RxGMSTask.just(userRef.setValue(dbUser));
            });
}
```

Fetch or Observe the value of a `Query` or `DatabaseReference` using the `RxQuery` utility
```java
public Observable<User> getUser(){
    return getUserRef()
            .flatMap(userRef -> RxQuery.observeSingleValue(userRef, User.class));
}
```

```java
RxQuery.observeValue(mPostReference, Post.class)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> {
            mAuthorView.setText(post.author);
            mTitleView.setText(post.title);
            mBodyView.setText(post.body);
        },
            error -> showToast("Error: " + error.getMessage())
        );
```

You can observe child events too
```java
RxQuery.observeChild(mDatabaseReference)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(event -> handleEvent(event),
                error -> mActivity.showToast("Error: " + error.getMessage()));
```

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Copyright 2017 Mehedi Hasan Khan
```
