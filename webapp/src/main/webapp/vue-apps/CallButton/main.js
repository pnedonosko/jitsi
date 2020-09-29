import JitsiMeetButton from "./components/JitsiMeetButton.vue";

Vue.use(Vuetify);
Vue.component("JitsiCallButton", JitsiMeetButton);
const vuetify = new Vuetify({
  dark: true,
  iconfont: ""
});

// getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || "en";
const localePortlet = "locale.jitsi";
const resourceBundleName = "Jitsi";
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${localePortlet}.${resourceBundleName}-${lang}.json`;

export function init(callSettings) {
  const process = new Promise(function (resolve, reject) {
    // getting locale ressources
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      // init Vue app when locale ressources are ready
      const thevue = new Vue({
        render : h =>
          h(JitsiMeetButton, {
            props : {
              ...callSettings,
              i18n : i18n,
              language : lang,
              resourceBundleName : resourceBundleName
            }
          }),
        i18n,
        vuetify
      });

      resolve(thevue);
    });
  });

  return process;
}

  