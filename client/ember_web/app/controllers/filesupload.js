import Ember from 'ember';
const { computed } = Ember;

export default Ember.Controller.extend({
    isAdmin: computed(function(){
        let binaryAdmin = Ember.$.cookie('is_administrator');
        if(binaryAdmin==1){
            return true;
        }
        return false;
    }),

    markets: computed(function(){
        let str = Ember.$.cookie('fu_data_markets');
        str = str.replace('[', '').replace(']', '').replace(/\"/g, "");
        let arr = str.split(',');
        return arr;
    })
});
