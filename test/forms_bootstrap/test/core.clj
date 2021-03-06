(ns forms-bootstrap.test.core
  (:use forms-bootstrap.core
        clojure.test
        forms-bootstrap.core
        noir.core
        net.cgrand.enlive-html)
  (:require [noir.response :as response]
            [sandbar.validation :as v]))

(def test-template "forms_bootstrap/test/test-page.html")

(deftemplate test-layout
  test-template
  [{:keys [form-tests links]}]
  [:p.example-link] (clone-for [[href link-name] links]
                                 [:a] (do-> (set-attr :href href)
                                            (content link-name)))
  [:section] (clone-for [[form-test header descr] form-tests]
                        [:div.page-header :h3] (html-content
                                                (str header " <small>" descr "</small"))
                        [:div.formhere] (content form-test)))

;;This first example uses the make-form function 
(defpage "/" [] 
  (test-layout {:links
                [["/make-form" "Make-Form Example"]
                 ["/form-helper" "Form-Helper Example"]]})) 

(defpage "/make-form" []
  (test-layout {:form-tests
                [[(make-form
                   :action "someaction"
                   :submit-label "Send it!"
                   :cancel-link "/"
                   :fields [{:type "text" :name "nickname" :label "Nick Name" :size "input-large"}
                            {:type "password" :name "password" :label "Password"}
                            {:type "text" :name "city" :label "City" :placeholder "Placeholder!"}
                            {:type "text-area" :name "description" :label "Favorite Quote"}
                            {:type "select" :name "colors" :label "Favorite Color"
                             :inputs [["blue" "Blue"]
                                      ["red" "Red"]
                                      ["yellow" "Yellow"]]}
                            {:type "radio" :name "cars" :label "Favorite Car"
                             :inputs [["honda" "Honda"]
                                      ["toyota" "Toyota"]
                                      ["chevy" "Chevy"]]}
                            {:type "checkbox" :name "languages" :label "Languages"
                             :inputs [["german" "German"]
                                      ["french" "French"]
                                      ["english" "English"]]}
                            {:type "file-input" :name "afile" :label "Choose a pic"}])
                  "Example One"
                  "How to use make-form"]]
                }))

(defn email-valid?
  [{:keys [email] :as m}]
  (if (= email "blah")
    (v/add-validation-error m :email "Your email cannot be 'blah'!")
    m))

(form-helper helper-example
             :validator (v/build-validator (v/non-empty-string :first-name)
                                           (v/non-empty-string :last-name)
                                           (v/non-nil :gender)
                                           (email-valid?))
             :post-url "/form-helper"
             :submit-label "Sign Up!"
             :fields [{:name "first-name"
                       :label "First Name"
                       :type "text"}
                      {:name "last-name"
                       :label "Last Name"
                       :type "text"}
                      {:name "gender"
                       :label "Gender"
                       :type "radio"
                       :inputs [["male" "Male"]
                                ["female" "Female"]]}
                      {:name "email"
                       :label "Email Address"
                       :type "text"
                       :placeholder "Try using 'blah'"}
                      {:name "colors[]"
                       :label "Favorite Colors"
                       :type "checkbox"
                       :inputs [["blue" "Blue"]
                                ["red" "Red"]
                                ["yellow" "Yellow"]
                                ["green" "Green"]]
                       :note "Pick 2 of the above colors!"}
                      {:name "username"
                       :label "Username"
                       :type "text"}
                      {:name "password"
                       :label "Password"
                       :type "password"}]
             :on-success (fn [{uname :username :as user-map}]
                           ;;on success actions here
                           (response/redirect "/"))
             :on-failure (fn [form-data]
                           ;;some failure action here
                           (render "/form-helper" form-data)))

;;This example shows how to access the entire request map. Typically
;;we just use 'm' from below, which is just the form params portion of
;;the map. Thats what you can pass in to your form function (ie
;;'helper-example') to populate default values in case of an
;;error. Alternatively, you could use a map with default values from a
;;database or some other data source to prepopulate your form.
(defpage "/form-helper"
  {:as m}
  (fn [req]
   ;; (println "Request map: " req)
    (test-layout
     {:form-tests
      [[(helper-example m "form-helper" "/")
        "Form-helper Example"
        "Uses the form-helper macro for easy validation."]]})))
